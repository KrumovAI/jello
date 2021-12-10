package uni.fmi.data.entity;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.Date;

@Entity
public class TaskEntity extends AbstractEntity {

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "deadline")
    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "list_id")
    private TaskListEntity list;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<LabelEntity> labels;

    public TaskEntity() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public TaskListEntity getList() {
        return list;
    }

    public void setList(TaskListEntity list) {
        this.list = list;
    }

    public Set<LabelEntity> getLabels() {
        return labels;
    }

    public void setLabels(Set<LabelEntity> labels) {
        this.labels = labels;
    }
}
