package com.example.application.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;

@Tag("sample-el")
@JsModule("./elements/sample.ts")
public class TestComponent extends LitTemplate {

    @Id
    private Paragraph name;

    public TestComponent(String name) {
        this.name.setText(name);
    }
}
