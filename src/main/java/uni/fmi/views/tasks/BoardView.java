package uni.fmi.views.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.componentfactory.multiselect.MultiComboBox;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.function.SerializablePredicate;
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
public class BoardView extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    private TaskService taskService;
    private TaskListService taskListService;
    private LabelService labelService;
    private Map<TaskListEntity, Grid<TaskEntity>> grids;
//    private List<Grid<TaskEntity>> grids;
//    private ListDataProvider<TaskEntity> dataProvider;
    private ListDataProvider<TaskListEntity> taskListDataProvider;
    private Label counter;
    private Collection<TaskEntity> tasks;
    private Collection<TaskListEntity> taskLists;

    public BoardView(@Autowired final TaskService taskService, @Autowired final TaskListService taskListService, @Autowired final LabelService labelService) {
        this.taskService = taskService;
        this.taskListService = taskListService;
        this.labelService = labelService;

        init();
    }

    private void init() {
        taskLists = taskListService.findAll();
        tasks = taskService.findAll();

        grids = new HashMap<TaskListEntity, Grid<TaskEntity>>();

        for (TaskListEntity list: taskLists) {
            var data = tasks.stream().filter(t -> t.getList().equals(list)).collect(Collectors.toList());
            ListDataProvider<TaskEntity> dataProvider = new ListDataProvider<TaskEntity>(data);
            Grid<TaskEntity> grid = new Grid<TaskEntity>(TaskEntity.class, false);

            grid.setDataProvider(dataProvider);
            grid.addItemDoubleClickListener(listener -> openTaskForm(listener.getItem(), list));

            grids.put(list, grid);

            resetCounter(list);

            final Column<TaskEntity> nameColumn = grid.addColumn(TaskEntity::getName);
            nameColumn.setHeader("Name");
            nameColumn.setSortable(true);
            nameColumn.setFooter(counter);

            final Column<TaskEntity> deadlineColumn = grid.addColumn(TaskEntity::getDeadline).setHeader("Deadline")
                    .setSortable(true);

            final Column<TaskEntity> labelsColumn = grid.addColumn(t -> String.join(", ", t.getLabels().stream().map(l -> l.getName()).collect(Collectors.toList()))).setHeader("Labels")
                    .setSortable(true);

            final HeaderRow headerRow = grid.appendHeaderRow();

            // name filter
            final TextField nameFilterField = new TextField();
            nameFilterField.addValueChangeListener(l -> {
                final String value = l.getValue();
                final SerializablePredicate<TaskEntity> filter = user -> StringUtils.containsIgnoreCase(user.getName(),
                        value);
                addFilter(filter, list);
            });
            nameFilterField.setWidthFull();
            headerRow.getCell(nameColumn).setComponent(nameFilterField);

            // email filter
//        final TextField emailFilterField = new TextField();
//        emailFilterField.setWidthFull();
//        headerRow.getCell(descriptionColumn).setComponent(emailFilterField);

            add(createButtons(list), grid);
        }
    }

    private HorizontalLayout createButtons(TaskListEntity list) {

        Grid<TaskEntity> grid = grids.get(list);

        final Button addButton = new Button("Add", l -> openTaskForm(new TaskEntity(), list));

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button edditButton = new Button("Edit", l -> openTaskForm(grid.asSingleSelect().getValue(), list));
        edditButton.setEnabled(false);
        edditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button removeButton = new Button("Remove", l -> {

            final Dialog dialog = new Dialog();

            final H1 titel = new H1("Are you sure?");
            final Button ok = new Button("OK", l1 -> {
                taskService.delete(grid.asSingleSelect().getValue().getId());
                resetGrid(list);
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

    private void openTaskForm(final TaskEntity newTask, TaskListEntity list) {
        Grid<TaskEntity> grid = grids.get(list);

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
            .bind(TaskEntity::getList, TaskEntity::setList);

        newTask.setList(list);
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
                resetGrid(list);

                if (!list.equals(newTask.getList())) {
                    resetGrid(newTask.getList());
                }

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

    private void resetGrid(TaskListEntity list) {
        Grid<TaskEntity> grid = grids.get(list);
        grid.select(null);
        ListDataProvider<TaskEntity> dataProvider = (ListDataProvider<TaskEntity>)grid.getDataProvider();
        dataProvider.clearFilters();
        list.getTasks().clear();
        list.getTasks().addAll(taskService.findAll().stream().filter(t -> t.getList().equals(list)).collect(Collectors.toList()));

        grid.setDataProvider(new ListDataProvider<TaskEntity>(list.getTasks()));
        dataProvider.refreshAll();
        resetCounter(list);
    }

    private void addFilter(final SerializablePredicate<TaskEntity> filter, TaskListEntity list) {
        Grid<TaskEntity> grid = grids.get(list);
        ListDataProvider<TaskEntity> dataProvider = (ListDataProvider<TaskEntity>)grid.getDataProvider();

        dataProvider.clearFilters();
        dataProvider.addFilter(filter);

        grid.setDataProvider(dataProvider);
        resetCounter(list);
    }

    private void resetCounter(TaskListEntity list) {
        Grid<TaskEntity> grid = grids.get(list);
        ListDataProvider<TaskEntity> dataProvider = (ListDataProvider<TaskEntity>)grid.getDataProvider();

        if (counter == null) {
            counter = new Label();
        }
        final Query<TaskEntity, SerializablePredicate<TaskEntity>> query = new Query<TaskEntity, SerializablePredicate<TaskEntity>>(
                dataProvider.getFilter());
        counter.setText("" + dataProvider.size(query));

    }

}
