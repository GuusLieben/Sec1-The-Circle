import {customElement, html, LitElement} from "lit-element";

@customElement('sample-el')
export class SampleElement extends LitElement {

    render() {
        return html`
            <div class="box" style="
                border: 1px solid #c9dbe6;
                margin: 0 0 16px;
                padding: 0 16px;
                cursor: pointer;
            ">
                <h3 id="name"></h3>
                <p id="author" style="font-style: italic"></p>
            </div>
  `
    }

}
