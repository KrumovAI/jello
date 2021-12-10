package uni.fmi.views.tasklists;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import uni.fmi.data.entity.TaskListEntity;
import uni.fmi.data.service.TaskListService;
import uni.fmi.views.MainLayout;

@PageTitle("TaskLists")
@Route(value = "task-lists", layout = MainLayout.class)
public class TaskListsView extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    private TaskListService taskListService;
    private Grid<TaskListEntity> grid;
    private ListDataProvider<TaskListEntity> dataProvider;
    private Label counter;
    private Collection<TaskListEntity> taskLists;

    public TaskListsView(@Autowired final TaskListService taskListService) {
        this.taskListService = taskListService;
        init();
    }

    private void init() {
        grid = new Grid<TaskListEntity>(TaskListEntity.class, false);

        taskLists = taskListService.findAll();
        dataProvider = new ListDataProvider<TaskListEntity>(taskLists);
        grid.setDataProvider(dataProvider);
        grid.addItemDoubleClickListener(listener -> openTaskListForm(listener.getItem()));
        resetCounter();
        final Column<TaskListEntity> nameColumn = grid.addColumn(TaskListEntity::getName);
        nameColumn.setHeader("Name");
        nameColumn.setSortable(true);
        nameColumn.setFooter(counter);

        final HeaderRow headerRow = grid.appendHeaderRow();

        // name filter
        final TextField nameFilterField = new TextField();
        nameFilterField.addValueChangeListener(l -> {
            final String value = l.getValue();
            final SerializablePredicate<TaskListEntity> filter = taskList -> StringUtils.containsIgnoreCase(taskList.getName(),
                    value);
            addFilter(filter);
        });
        nameFilterField.setWidthFull();
        headerRow.getCell(nameColumn).setComponent(nameFilterField);

        add(createButtons(), grid);
    }

    private HorizontalLayout createButtons() {

        final Button addButton = new Button("Add", l -> openTaskListForm(new TaskListEntity()));

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button edditButton = new Button("Edit", l -> openTaskListForm(grid.asSingleSelect().getValue()));
        edditButton.setEnabled(false);
        edditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button removeButton = new Button("Remove", l -> {

            final Dialog dialog = new Dialog();

            final H1 titel = new H1("Are you sure?");
            final Button ok = new Button("OK", l1 -> {
                taskListService.delete(grid.asSingleSelect().getValue().getId());
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

        final SingleSelect<Grid<TaskListEntity>, TaskListEntity> asSingleSelect = grid.asSingleSelect();
        asSingleSelect.addValueChangeListener(l -> {
            final TaskListEntity value = l.getValue();
            removeButton.setEnabled(value != null);
            edditButton.setEnabled(value != null);
        });

        final HorizontalLayout buttons = new HorizontalLayout(addButton, edditButton, removeButton);
        return buttons;
    }

    private void openTaskListForm(final TaskListEntity newTaskList) {
        final Dialog dialog = new Dialog();
        final TextField name = new TextField();

        final BeanValidationBinder<TaskListEntity> binder = new BeanValidationBinder<>(TaskListEntity.class);

        binder.bind(name, TaskListEntity::getName, TaskListEntity::setName);

        binder.forField(name)
                .asRequired()
                .bind(TaskListEntity::getName, TaskListEntity::setName);

        binder.readBean(newTaskList);
        final FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, "Name");

        final Button ok = new Button("OK", l1 -> {

            boolean beanIsValid = binder.writeBeanIfValid(newTaskList);
            if (beanIsValid) {
                taskListService.update(newTaskList);
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
        taskLists.clear();
        taskLists.addAll(taskListService.findAll());
        dataProvider.refreshAll();
        resetCounter();
    }

    private void addFilter(final SerializablePredicate<TaskListEntity> filter) {
        dataProvider.clearFilters();
        dataProvider.addFilter(filter);
        resetCounter();
    }

    private void resetCounter() {
        if (counter == null) {
            counter = new Label();
        }
        final Query<TaskListEntity, SerializablePredicate<TaskListEntity>> query = new Query<TaskListEntity, SerializablePredicate<TaskListEntity>>(
                dataProvider.getFilter());
        counter.setText("" + dataProvider.size(query));

    }

}
