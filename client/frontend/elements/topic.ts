import {customElement, html, LitElement} from "lit-element";

@customElement('topic-component')
export class TopicElement extends LitElement {

    render() {
        return html`
            <div class="box" style="
                border: 1px solid #c9dbe6;
                margin: 0 0 16px;
                padding: 0 16px;
            ">
                <h3 id="name" style="cursor: pointer;"></h3>
                <p id="author" style="font-style: italic"></p>
            </div>
  `
    }

}
