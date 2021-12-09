package uni.fmi.views.tasks;

import com.vaadin.componentfactory.multiselect.MultiComboBox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uni.fmi.data.entity.LabelEntity;
import uni.fmi.data.entity.TaskEntity;
import uni.fmi.data.entity.TaskListEntity;
import uni.fmi.data.service.LabelService;
import uni.fmi.data.service.TaskListService;
import uni.fmi.data.service.TaskService;
import uni.fmi.views.MainLayout;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Tasks")
@Route(value = "tasks", layout = MainLayout.class)
public class TasksView extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    private TaskService taskService;
    private TaskListService taskListService;
    private LabelService labelService;
    private Grid<TaskEntity> grid;
    private ListDataProvider<TaskEntity> dataProvider;
    private Label counter;
    private Collection<TaskEntity> tasks;
    private Map<String, SerializablePredicate<TaskEntity>> filters;

    public TasksView(@Autowired final TaskService taskService, @Autowired final TaskListService taskListService, @Autowired final LabelService labelService) {
        this.taskService = taskService;
        this.taskListService = taskListService;
        this.labelService = labelService;

        init();
    }

    private void init() {
        grid = new Grid<TaskEntity>(TaskEntity.class, false);

        filters = new HashMap<String, SerializablePredicate<TaskEntity>>();

        tasks = taskService.findAll();
        dataProvider = new ListDataProvider<TaskEntity>(tasks);
        grid.setDataProvider(dataProvider);
        grid.addItemDoubleClickListener(listener -> openTaskForm(listener.getItem()));
        resetCounter();
        final Column<TaskEntity> nameColumn = grid.addColumn(TaskEntity::getName);
        nameColumn.setHeader("Name");
        nameColumn.setSortable(true);
        nameColumn.setFooter(counter);

        final Column<TaskEntity> deadlineColumn = grid.addColumn(TaskEntity::getDeadline).setHeader("Deadline")
                .setSortable(true);

        final Column<TaskEntity> labelsColumn = grid.addColumn(t -> String.join(", ", t.getLabels().stream().map(l -> l.getName()).collect(Collectors.toList()))).setHeader("Labels")
                .setSortable(true);

        final Column<TaskEntity> taskListColumn = grid.addColumn(t -> t.getList().getName()).setHeader("Task List")
                .setSortable(true);

        final HeaderRow headerRow = grid.appendHeaderRow();

        // name filter
        final TextField nameFilterField = new TextField();
        nameFilterField.addValueChangeListener(l -> {
            final String value = l.getValue();
            filters.put("name", task -> StringUtils.containsIgnoreCase(task.getName(), value));
            refreshFilter();
        });
        nameFilterField.setWidthFull();
        headerRow.getCell(nameColumn).setComponent(nameFilterField);

        // deadline filter
        final DatePicker deadlineFilterField = new DatePicker();
        deadlineFilterField.addValueChangeListener(l -> {
            final LocalDate value = l.getValue();
            filters.put("deadline", task -> value == null || task.getDeadline().equals(value));
            refreshFilter();
        });
        deadlineFilterField.setWidthFull();
        headerRow.getCell(deadlineColumn).setComponent(deadlineFilterField);

        // labels filter
        final MultiComboBox<LabelEntity> labelsFilterField = new MultiComboBox<LabelEntity>();

        labelsFilterField.setDataProvider(labelService::fetchItems, labelService::count);
        labelsFilterField.setItemLabelGenerator(LabelEntity::getName);

        labelsFilterField.addValueChangeListener(l -> {
            final Set<LabelEntity> value = l.getValue();
            filters.put("labels", task -> task.getLabels().containsAll(value));
            refreshFilter();
        });
        labelsFilterField.setWidthFull();
        headerRow.getCell(labelsColumn).setComponent(labelsFilterField);

        // task lists filter
        final ComboBox<TaskListEntity> taskListFilterField = new ComboBox<TaskListEntity>();

        taskListFilterField.setDataProvider(taskListService::fetchItems, taskListService::count);
        taskListFilterField.setItemLabelGenerator(TaskListEntity::getName);

        taskListFilterField.addValueChangeListener(l -> {
            final TaskListEntity value = l.getValue();
            filters.put("taskList", task -> task.getList().equals(value));
            refreshFilter();
        });
        taskListFilterField.setWidthFull();
        headerRow.getCell(taskListColumn).setComponent(taskListFilterField);

        add(createButtons(), grid);
    }

    private HorizontalLayout createButtons() {

        final Button addButton = new Button("Add", l -> openTaskForm(new TaskEntity()));

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button edditButton = new Button("Edit", l -> openTaskForm(grid.asSingleSelect().getValue()));
        edditButton.setEnabled(false);
        edditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button removeButton = new Button("Remove", l -> {

            final Dialog dialog = new Dialog();

            final H1 titel = new H1("Are you sure?");
            final Button ok = new Button("OK", l1 -> {
                taskService.delete(grid.asSingleSelect().getValue().getId());
                resetGrid();
                dialog.close();
            });
            final Button close = new Button("Close", l1 -> dialog.close());
            final HorizontalLayout dialogButtons = new HorizontalLayout(ok, close);
            final VerticalLayout dialogBody = new VerticalLayout(titel, dialogButtons);
            dialog.add(dialogBody);
            dialog.open();

        });
        removeButton.setEnabled(false);
        removeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final SingleSelect<Grid<TaskEntity>, TaskEntity> asSingleSelect = grid.asSingleSelect();
        asSingleSelect.addValueChangeListener(l -> {
            final TaskEntity value = l.getValue();
            removeButton.setEnabled(value != null);
            edditButton.setEnabled(value != null);
        });

        final HorizontalLayout buttons = new HorizontalLayout(addButton, edditButton, removeButton);
        return buttons;
    }

    private void openTaskForm(final TaskEntity newTask) {
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

        binder.readBean(newTask);

        final FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, "Name");
        formLayout.addFormItem(description, "Description");
        formLayout.addFormItem(deadline, "Deadline");
        formLayout.addFormItem(labelsCombo, "Labels");
        formLayout.addFormItem(taskListsCombo, "Task List");

        final Button ok = new Button("OK", l1 -> {

            boolean beanIsValid = binder.writeBeanIfValid(newTask);
            if (beanIsValid) {
                taskService.update(newTask);
                resetGrid();

                dialog.close();
            }
        });

        final Button close = new Button("Close", l1 -> dialog.close());
        final HorizontalLayout dialogButtons = new HorizontalLayout(ok, close);
        final VerticalLayout dialogBody = new VerticalLayout(formLayout, dialogButtons);
        dialogBody.expand(formLayout);
        dialogBody.setSizeFull();
        dialog.setWidth("400px");
        dialog.setHeight("400px");

        dialog.add(dialogBody);
        dialog.open();
    }

    private void resetGrid() {
        grid.select(null);
        dataProvider.clearFilters();
        tasks.clear();
        tasks.addAll(taskService.findAll());
        dataProvider.refreshAll();
        resetCounter();
    }

    private void refreshFilter() {
        dataProvider.clearFilters();

        for (Map.Entry<String, SerializablePredicate<TaskEntity>> set : filters.entrySet()) {
            dataProvider.addFilter(set.getValue());
        }

        resetCounter();
    }

    private void resetCounter() {
        if (counter == null) {
            counter = new Label();
        }
        final Query<TaskEntity, SerializablePredicate<TaskEntity>> query = new Query<TaskEntity, SerializablePredicate<TaskEntity>>(
                dataProvider.getFilter());
        counter.setText("" + dataProvider.size(query));

    }

}
