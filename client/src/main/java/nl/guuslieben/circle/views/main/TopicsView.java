package nl.guuslieben.circle.views.main;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.util.List;

import nl.guuslieben.circle.ClientService;
import nl.guuslieben.circle.ServerResponse;
import nl.guuslieben.circle.common.Topic;
import nl.guuslieben.circle.components.TopicComponent;
import nl.guuslieben.circle.components.TopicDetailsComponent;
import nl.guuslieben.circle.views.MainLayout;

@Route(value = "topics", layout = MainLayout.class)
@RouteAlias(value = "topics", layout = MainLayout.class)
@PageTitle("The Circle - Topics")
@Tag("topics-view")
@JsModule("./views/topics-view.ts")
public class TopicsView extends LitTemplate {

    private final transient ClientService service;

    @Id
    private Div content;

    private TextField name;
    private final Div topics;
    private final TopicDetailsComponent details;

    public TopicsView(ClientService service) {
        this.service = service;
        this.topics = new Div();
        this.details = new TopicDetailsComponent(service);
        this.onGet();
    }

    public void enableCreating(boolean enable) {
        if (enable) {
            this.name = new TextField("Create topic");
            this.content.add(this.name);

            Button create = new Button("Create");
            create.addClickListener(this::onCreate);
            this.content.add(create);
        }

        this.details.enableCreating(enable);

        SplitLayout layout = new SplitLayout(this.topics, this.details);
        this.content.add(layout);
    }

    private void onGet() {
        final List<Topic> topics = this.service.getTopics();
        Notification.show("Found " + topics.size() + " topics");
        this.topics.removeAll();
        for (Topic topic : topics) {
            this.topics.add(new TopicComponent(this.service, topic, this.details::setTopic));
        }
    }

    public void onCreate(ClickEvent<Button> event) {
        final ServerResponse<Topic> response = this.service.createTopic(this.name.getValue());
        this.name.clear();

        if (response.accepted()) {
            final Topic topic = response.getObject();
            Notification.show("Created topic '" + topic.getName() + "' with ID " + topic.getId());
            this.onGet();
        } else {
            Notification.show("Could not create topic: " + response.getMessage());
        }
    }
}
