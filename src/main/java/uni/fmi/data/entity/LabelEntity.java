package uni.fmi.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.util.Set;

@Entity
public class LabelEntity extends AbstractEntity {

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<TaskEntity> tasks;

    public LabelEntity(String name) {
        this.setName(name);
    }

    public LabelEntity() {

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
