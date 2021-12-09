package uni.fmi.views.users;

import java.util.Collection;

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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import uni.fmi.data.entity.UserEntity;
import uni.fmi.data.service.UserService;
import uni.fmi.views.MainLayout;

@PageTitle("Users")
@Route(value = "users", layout = MainLayout.class)
public class UserOverview extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	private UserService userService;
	private Grid<UserEntity> grid;
	private ListDataProvider<UserEntity> dataProvider;
	private Label counter;
	private Collection<UserEntity> users;

	public UserOverview(@Autowired final UserService userService) {
		this.userService = userService;
		init();
	}

	private void init() {
		grid = new Grid<UserEntity>(UserEntity.class, false);

		users = userService.findAll();
		dataProvider = new ListDataProvider<UserEntity>(users);
		grid.setDataProvider(dataProvider);
		grid.addItemDoubleClickListener(listener -> openUserForm(listener.getItem()));
		resetCounter();
		final Column<UserEntity> nameColumn = grid.addColumn(UserEntity::getUsername);
		nameColumn.setHeader("Name");
		nameColumn.setSortable(true);
		nameColumn.setFooter(counter);

		final Column<UserEntity> emailColumn = grid.addColumn(UserEntity::getEmail).setHeader("Email")
				.setSortable(true);

		final HeaderRow headerRow = grid.appendHeaderRow();

		// name filter
		final TextField nameFilterField = new TextField();
		nameFilterField.addValueChangeListener(l -> {
			final String value = l.getValue();
			final SerializablePredicate<UserEntity> filter = user -> StringUtils.containsIgnoreCase(user.getUsername(),
					value);
			addFilter(filter);
		});
		nameFilterField.setWidthFull();
		headerRow.getCell(nameColumn).setComponent(nameFilterField);

		// email filter
		final TextField emailFilterField = new TextField();
		emailFilterField.setWidthFull();
		headerRow.getCell(emailColumn).setComponent(emailFilterField);

		add(createButtons(), grid);
	}

	private HorizontalLayout createButtons() {

		final Button addButton = new Button("Add", l -> openUserForm(new UserEntity()));

		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		final Button edditButton = new Button("Edit", l -> openUserForm(grid.asSingleSelect().getValue()));
		edditButton.setEnabled(false);
		edditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		final Button removeButton = new Button("Remove", l -> {

			final Dialog dialog = new Dialog();

			final H1 titel = new H1("Сигурен ли сте?");
			final Button ok = new Button("OK", l1 -> {
				userService.delete(grid.asSingleSelect().getValue().getId());
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

		final SingleSelect<Grid<UserEntity>, UserEntity> asSingleSelect = grid.asSingleSelect();
		asSingleSelect.addValueChangeListener(l -> {
			final UserEntity value = l.getValue();
			removeButton.setEnabled(value != null);
			edditButton.setEnabled(value != null);
		});

		final HorizontalLayout buttons = new HorizontalLayout(addButton, edditButton, removeButton);
		return buttons;
	}

	private void openUserForm(final UserEntity newUser) {
		final Dialog dialog = new Dialog();
		final TextField username = new TextField();
		final PasswordField pass1 = new PasswordField();
		final PasswordField pass2 = new PasswordField();
		final EmailField email = new EmailField();
		final TextField avatarLocation = new TextField();

		final ComboBox<UserEntity> usersCombo = new ComboBox<UserEntity>();
		usersCombo.setDataProvider(userService::fetchItems, userService::count);
		usersCombo.setItemLabelGenerator(UserEntity::getUsername);
		final BeanValidationBinder<UserEntity> binder = new BeanValidationBinder<>(UserEntity.class);

		binder.bind(username, UserEntity::getUsername, UserEntity::setUsername);
		binder.bind(pass1, UserEntity::getPassword, UserEntity::setPassword);
		binder.bind(email, UserEntity::getEmail, UserEntity::setEmail);
		binder.bind(avatarLocation, "avatarLocation");
		binder.readBean(newUser);
		final FormLayout formLayout = new FormLayout();
		formLayout.addFormItem(username, "User Name");
		formLayout.addFormItem(pass1, "Password");
		formLayout.addFormItem(pass2, "Password2");
		formLayout.addFormItem(email, "Email");
		formLayout.addFormItem(avatarLocation, "Avatar Location");
		formLayout.addFormItem(usersCombo, "Users");

		final Button ok = new Button("OK", l1 -> {

			boolean beanIsValid = binder.writeBeanIfValid(newUser);
			if (beanIsValid) {
				userService.update(newUser);
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
		users.clear();
		users.addAll(userService.findAll());
		dataProvider.refreshAll();
		resetCounter();
	}

	private void addFilter(final SerializablePredicate<UserEntity> filter) {
		dataProvider.clearFilters();
		dataProvider.addFilter(filter);
		resetCounter();
	}

	private void resetCounter() {
		if (counter == null) {
			counter = new Label();
		}
		final Query<UserEntity, SerializablePredicate<UserEntity>> query = new Query<UserEntity, SerializablePredicate<UserEntity>>(
				dataProvider.getFilter());
		counter.setText("" + dataProvider.size(query));

	}

}
