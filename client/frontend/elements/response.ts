import {customElement, html, LitElement} from "lit-element";

@customElement('response-component')
export class ResponseElement extends LitElement {

    render() {
        return html`
            <p id="content"></p>
  `
    }

}
