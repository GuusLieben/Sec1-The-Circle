package nl.guuslieben.circle.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;

import java.util.Optional;

import nl.guuslieben.circle.views.main.TopicsView;
import nl.guuslieben.circle.views.main.LoginView;
import nl.guuslieben.circle.views.main.MainView;
import nl.guuslieben.circle.views.main.RegisterView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final Tabs menu;
    private H1 viewTitle;
    private Avatar avatar;
    private boolean enabled;

    public MainLayout() {
        this.setPrimarySection(Section.DRAWER);
        this.addToNavbar(true, this.createHeaderContent());
        this.menu = this.createMenu();
        this.addToDrawer(this.createDrawerContent(this.menu));
    }

    public void setAvatarName(String name) {
        this.avatar.setName(name);
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setClassName("sidemenu-header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        this.viewTitle = new H1();
        layout.add(this.viewTitle);

        this.avatar = new Avatar();
        this.avatar.addClassNames("ms-auto", "me-m");
        layout.add(this.avatar);
        return layout;
    }

    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.setClassName("sidemenu-menu");
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new Image("images/logo.png", "The Circle logo"));
        logoLayout.add(new H1("The Circle"));
        layout.add(logoLayout, menu);

        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(this.createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[]{
                createTab("Home", MainView.class),
                createTab("Login", LoginView.class),
                createTab("Register", RegisterView.class),
                createTab("Create Topic", TopicsView.class),
        };
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        this.getTabForComponent(this.getContent()).ifPresent(this.menu::setSelectedTab);
        this.viewTitle.setText(this.getCurrentPageTitle());
        if (this.getContent() instanceof LoginView) ((LoginView) this.getContent()).setAfterLogin(data -> {
            this.setAvatarName(data.getName());
            this.enabled = true;
        });
        if (this.getContent() instanceof TopicsView) ((TopicsView) this.getContent()).enableCreating(this.enabled);
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return this.menu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        PageTitle title = this.getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
