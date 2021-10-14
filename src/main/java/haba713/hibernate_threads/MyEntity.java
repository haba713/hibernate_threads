package haba713.hibernate_threads;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column
    private Integer myColumn;

    public Integer getMyColumn() {
        return myColumn;
    }

    public void setMyColumn(Integer myColumn) {
        this.myColumn = myColumn;
    }

}
