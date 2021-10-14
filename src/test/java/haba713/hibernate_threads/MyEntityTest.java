package haba713.hibernate_threads;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MyEntityTest {

	private static final int WORKER_COUNT = 4;
	private static final int UPDATE_COUNT = 5;

	private SessionFactory sessionFactory;

	@Before
	public void setUp() throws Exception {
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	@After
	public void tearDown() throws Exception {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	@Test
	public void test() throws Exception {

		// Create one db row.
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		if (session.createQuery("from MyEntity").list().isEmpty()) {
			MyEntity myEntity = new MyEntity();
			myEntity.setMyColumn(0);
			session.save(myEntity);
		}
		session.getTransaction().commit();
		session.close();

		// Update the row simultaneously with multiple workers.
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < WORKER_COUNT; i++) {
			threads.add(new Thread(new Worker()));
		}
		threads.stream().forEach(Thread::start);
		for (Thread thread : threads)
			thread.join();

			synchronized (Worker.class) {
				while (mutex) {
					Thread.yield();
				}
			}

		// Check the result.
		session = sessionFactory.openSession();
		System.out.printf("thread-%d: assert\n", Thread.currentThread().getId());
		session.beginTransaction();
		MyEntity myEntity = (MyEntity) session.createQuery("from MyEntity").list().get(0);
		assertEquals(WORKER_COUNT * UPDATE_COUNT, myEntity.getMyColumn().intValue());
		session.getTransaction().commit();

		session.close();
	}

	private static boolean mutex = false;

	private class Worker implements Runnable {

		@Override
		public void run() {
			Session session = sessionFactory.openSession();
			for (int i = 0; i < UPDATE_COUNT; i++) {
				plusOne(session, Thread.currentThread().getId());
			}
			session.close();
		}

		private void plusOne(Session session, long threadId) {
			synchronized (Worker.class) {
				while (mutex) {
					Thread.yield();
				}
				mutex = true;
				System.out.printf("thread-%d: mutex lock, begin, update\n", threadId);
				session.beginTransaction();
				MyEntity myEntity = (MyEntity) session.createQuery("from MyEntity").list().get(0);
				int oldValue = myEntity.getMyColumn();
				int newValue = oldValue + 1;
				myEntity.setMyColumn(newValue);
				session.save(myEntity);
				System.out.printf("thread-%d: %d + 1 = %d\n", threadId, oldValue, newValue);
				session.getTransaction().commit();
				System.out.printf("thread-%d: commit, mutex release\n", threadId);
				mutex = false;
			}
		}

	}

}
