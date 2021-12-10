package uni.fmi.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class TaskListEntity extends AbstractEntity {

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<TaskEntity> tasks;

    public TaskListEntity(String name) {
        this.setName(name);
    }

    public TaskListEntity() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<TaskEntity> getTasks() {
        return tasks;
    }
}
