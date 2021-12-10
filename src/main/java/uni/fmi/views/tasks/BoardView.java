package uni.fmi.views.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.componentfactory.multiselect.MultiComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.dom.ElementAttachListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import uni.fmi.data.entity.LabelEntity;
import uni.fmi.data.entity.TaskEntity;
import uni.fmi.data.entity.TaskListEntity;
import uni.fmi.data.service.LabelService;
import uni.fmi.data.service.TaskListService;
import uni.fmi.data.service.TaskService;
import uni.fmi.views.MainLayout;

@PageTitle("Board")
@Route(value = "", layout = MainLayout.class)
public class BoardView extends HorizontalLayout {
    private static final long serialVersionUID = 1L;
    private TaskService taskService;
    private TaskListService taskListService;
    private LabelService labelService;
    private Label counter;
    private Collection<TaskEntity> tasks;
    private Collection<TaskListEntity> taskLists;

    private Map<TaskListEntity, VerticalLayout> listLayouts;

    public BoardView(@Autowired final TaskService taskService, @Autowired final TaskListService taskListService, @Autowired final LabelService labelService) {
        this.taskService = taskService;
        this.taskListService = taskListService;
        this.labelService = labelService;

        init();
    }

    private void init() {
        taskLists = taskListService.findAll();
        listLayouts = new HashMap<>();

        this.addClassName("board");

        for (TaskListEntity list: taskLists) {
            VerticalLayout listLayout = new VerticalLayout();
            listLayout.addClassName("task-list");

            add(listLayout);
            listLayouts.put(list, listLayout);
        }

        populateBoard();
    }

    private void populateBoard() {
        tasks = taskService.findAll();

        for (TaskListEntity list: taskLists) {
            VerticalLayout layout = listLayouts.get(list);

            layout.removeAll();

            H2 listHeading = new H2(list.getName());
            listHeading.addClassName("task-list-heading");

            this.listLayouts.put(list, layout);
            add(layout);

            layout.add(listHeading);

            final List<TaskEntity> listTasks = tasks.stream().filter(t -> t.getList().equals(list)).collect(Collectors.toList());

            for (TaskEntity task: listTasks) {
                TaskCardView taskCard = new TaskCardView(task);
                taskCard.addClickListener(listener -> openTaskForm(task));

                layout.add(taskCard);
            }

            TaskEntity newTask = new TaskEntity();
            newTask.setList(list);

            Button addButton = new Button("Add", l -> openTaskForm(newTask));
            addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addButton.addClassName("task-list-add-btn");

            layout.add(addButton);
        }
    }

    private void openTaskForm(final TaskEntity task) {

        final Dialog dialog = new Dialog();
        final TextField name = new TextField();
        final TextArea description = new TextArea();
        final DatePicker deadline = new DatePicker();

        final MultiComboBox<LabelEntity> labelsCombo = new MultiComboBox<LabelEntity>();
        labelsCombo.setDataProvider(labelService::fetchItems, labelService::count);
        labelsCombo.setItemLabelGenerator(LabelEntity::getName);

        final ComboBox<TaskListEntity> taskListsCombo = new ComboBox<TaskListEntity>();
        taskListsCombo.setDataProvider(taskListService::fetchItems, taskListService::count);
        taskListsCombo.setItemLabelGenerator(TaskListEntity::getName);

        final BeanValidationBinder<TaskEntity> binder = new BeanValidationBinder<>(TaskEntity.class);

        binder.forField(name)
            .asRequired()
            .bind(TaskEntity::getName, TaskEntity::setName);

        binder.forField(description)
            .bind(TaskEntity::getDescription, TaskEntity::setDescription);

        binder.forField(deadline)
            .bind(TaskEntity::getDeadline, TaskEntity::setDeadline);

        binder.forField(labelsCombo)
            .bind(TaskEntity::getLabels, TaskEntity::setLabels);

        binder.forField(labelsCombo)
            .bind(TaskEntity::getLabels, TaskEntity::setLabels);

        binder.forField(taskListsCombo)
            .asRequired()
            .bind(TaskEntity::getList, TaskEntity::setList);

        binder.readBean(task);

        final FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, "Name");
        formLayout.addFormItem(description, "Description");
        formLayout.addFormItem(deadline, "Deadline");
        formLayout.addFormItem(labelsCombo, "Labels");
        formLayout.addFormItem(taskListsCombo, "Task List");

        final HorizontalLayout dialogButtons = new HorizontalLayout();

        final Button ok = new Button("OK", l1 -> {

            boolean beanIsValid = binder.writeBeanIfValid(task);
            if (beanIsValid) {
                taskService.update(task);
                populateBoard();
                dialog.close();
            }
        });

        dialogButtons.add(ok);

        if (task.getId() != null) {
            final Button delete = new Button("Remove", l -> {

                final Dialog deleteDialog = new Dialog();

                final H1 title = new H1("Are you sure?");
                final Button deleteOk = new Button("OK", l1 -> {
                    taskService.delete(task.getId());
                    populateBoard();

                    deleteDialog.close();
                    dialog.close();
                });

                final Button deleteClose = new Button("Close", l1 -> dialog.close());
                final HorizontalLayout deleteDialogButtons = new HorizontalLayout(deleteOk, deleteClose);
                final VerticalLayout dialogBody = new VerticalLayout(title, deleteDialogButtons);

                deleteDialog.add(dialogBody);
                deleteDialog.open();
            });

            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            dialogButtons.add(delete);
        }

        final Button close = new Button("Close", l1 -> dialog.close());
        dialogButtons.add(close);

        final VerticalLayout dialogBody = new VerticalLayout(formLayout, dialogButtons);
        dialogBody.expand(formLayout);
        dialogBody.setSizeFull();
        dialog.setWidth("400px");
        dialog.setHeight("400px");

        dialog.add(dialogBody);
        dialog.open();
    }

}
