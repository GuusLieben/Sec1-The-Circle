package nl.guuslieben.circle.views.main;

import nl.guuslieben.circle.components.TestComponent;
import nl.guuslieben.circle.views.MainLayout;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

/**
 * A Designer generated component for the stub-tag template.
 *
 * Designer will add and remove fields with @Id mappings but does not overwrite
 * or otherwise change this file.
 */
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("The Circle")
@Tag("main-view")
@JsModule("./views/main-view.ts")
public class MainView extends LitTemplate {

    @Id
    private TextField name;
    @Id
    private Button sayHello;
    @Id
    private Div list;

    public MainView() {
        this.sayHello.addClickListener(e -> {
            this.list.add(new TestComponent(this.name.getValue()));
            Notification.show("Hello " + this.name.getValue());
        });
    }
}
