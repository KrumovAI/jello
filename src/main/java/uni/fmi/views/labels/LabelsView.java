package uni.fmi.views.labels;

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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import uni.fmi.data.entity.LabelEntity;
import uni.fmi.data.entity.LabelEntity;
import uni.fmi.data.service.LabelService;
import uni.fmi.views.MainLayout;

@PageTitle("Labels")
@Route(value = "labels", layout = MainLayout.class)
public class LabelsView extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    private LabelService labelService;
    private Grid<LabelEntity> grid;
    private ListDataProvider<LabelEntity> dataProvider;
    private Label counter;
    private Collection<LabelEntity> labels;

    public LabelsView(@Autowired final LabelService labelService) {
        this.labelService = labelService;
        init();
    }

    private void init() {
        grid = new Grid<LabelEntity>(LabelEntity.class, false);

        labels = labelService.findAll();
        dataProvider = new ListDataProvider<LabelEntity>(labels);
        grid.setDataProvider(dataProvider);
        grid.addItemDoubleClickListener(listener -> openLabelForm(listener.getItem()));
        resetCounter();
        final Column<LabelEntity> nameColumn = grid.addColumn(LabelEntity::getName);
        nameColumn.setHeader("Name");
        nameColumn.setSortable(true);
        nameColumn.setFooter(counter);

        final HeaderRow headerRow = grid.appendHeaderRow();

        // name filter
        final TextField nameFilterField = new TextField();
        nameFilterField.addValueChangeListener(l -> {
            final String value = l.getValue();
            final SerializablePredicate<LabelEntity> filter = label -> StringUtils.containsIgnoreCase(label.getName(),
                    value);
            addFilter(filter);
        });
        nameFilterField.setWidthFull();
        headerRow.getCell(nameColumn).setComponent(nameFilterField);

        add(createButtons(), grid);
    }

    private HorizontalLayout createButtons() {

        final Button addButton = new Button("Add", l -> openLabelForm(new LabelEntity()));

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button edditButton = new Button("Edit", l -> openLabelForm(grid.asSingleSelect().getValue()));
        edditButton.setEnabled(false);
        edditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button removeButton = new Button("Remove", l -> {

            final Dialog dialog = new Dialog();

            final H1 titel = new H1("Are you sure?");
            final Button ok = new Button("OK", l1 -> {
                labelService.delete(grid.asSingleSelect().getValue().getId());
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

        final SingleSelect<Grid<LabelEntity>, LabelEntity> asSingleSelect = grid.asSingleSelect();
        asSingleSelect.addValueChangeListener(l -> {
            final LabelEntity value = l.getValue();
            removeButton.setEnabled(value != null);
            edditButton.setEnabled(value != null);
        });

        final HorizontalLayout buttons = new HorizontalLayout(addButton, edditButton, removeButton);
        return buttons;
    }

    private void openLabelForm(final LabelEntity newLabel) {
        final Dialog dialog = new Dialog();
        final TextField name = new TextField();

        final BeanValidationBinder<LabelEntity> binder = new BeanValidationBinder<>(LabelEntity.class);

        binder.bind(name, LabelEntity::getName, LabelEntity::setName);

        binder.forField(name)
            .asRequired()
            .bind(LabelEntity::getName, LabelEntity::setName);

        binder.readBean(newLabel);
        final FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, "Name");

        final Button ok = new Button("OK", l1 -> {

            boolean beanIsValid = binder.writeBeanIfValid(newLabel);
            if (beanIsValid) {
                labelService.update(newLabel);
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
        labels.clear();
        labels.addAll(labelService.findAll());
        dataProvider.refreshAll();
        resetCounter();
    }

    private void addFilter(final SerializablePredicate<LabelEntity> filter) {
        dataProvider.clearFilters();
        dataProvider.addFilter(filter);
        resetCounter();
    }

    private void resetCounter() {
        if (counter == null) {
            counter = new Label();
        }
        final Query<LabelEntity, SerializablePredicate<LabelEntity>> query = new Query<LabelEntity, SerializablePredicate<LabelEntity>>(
                dataProvider.getFilter());
        counter.setText("" + dataProvider.size(query));

    }

}
