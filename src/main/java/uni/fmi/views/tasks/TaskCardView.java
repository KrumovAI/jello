package uni.fmi.views.tasks;

import com.vaadin.flow.component.html.*;
import uni.fmi.data.entity.LabelEntity;
import uni.fmi.data.entity.TaskEntity;

public class TaskCardView extends Div {

    private TaskEntity task;

    public TaskCardView(final TaskEntity task) {
        this.task = task;

        this.init();
    }

    private void init() {
        this.addClassName("card");

        H3 titleHeading = new H3(this.task.getName());
        titleHeading.addClassName("card-title");
        this.add(titleHeading);

        if (this.task.getDeadline() != null) {
            Paragraph deadlineParagraph = new Paragraph(this.task.getDeadline().toString());
            deadlineParagraph.addClassName("card-date");
            this.add(deadlineParagraph);
        }

        Paragraph descriptionParagraph = new Paragraph(this.task.getDescription());
        descriptionParagraph.addClassName("card-text-content");
        this.add(descriptionParagraph);

        if (this.task.getLabels().size() > 0) {
            Div labelContainer = new Div();
            labelContainer.addClassName("card-label-container");

            for (LabelEntity label : this.task.getLabels()) {
                Span labelSpan = new Span(label.getName());
                labelSpan.addClassName("card-label");

                labelContainer.add(labelSpan);
            }

            this.add(labelContainer);
        }
    }
}
