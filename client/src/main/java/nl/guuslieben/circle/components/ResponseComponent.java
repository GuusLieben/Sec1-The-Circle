package nl.guuslieben.circle.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;

import nl.guuslieben.circle.common.Response;

@Tag("response-component")
@JsModule("./elements/response.ts")
public class ResponseComponent extends LitTemplate {

    @Id
    private Paragraph content;

    public ResponseComponent(Response response) {
        Label label = new Label();
        label.getElement().setProperty("innerHTML", "<b>%s (%s) said:</b> %s".formatted(response.getAuthor().getName(), response.getAuthor().getEmail(), response.getContent()));
        this.content.add(label);
    }
}
