package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.EdgePresentation;
import SW9.utility.mouse.MouseTracker;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ComponentController implements Initializable {

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);

    public BorderPane toolbar;
    public Rectangle background;
    public TextArea declaration;
    public JFXButton toggleDeclarationButton;
    public BorderPane frame;
    public JFXTextField name;
    public StackPane root;
    public Pane modelContainer;
    public Line line1;
    public Line line2;
    public Label x;
    public Label y;
    public Pane defaultLocationsContainer;

    private MouseTracker mouseTracker;

    private Map<Edge, EdgePresentation> edgePresentationMap = new HashMap<>();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        component.addListener((observable, oldValue, newValue) -> {
            // Bind the width and the height of the abstraction to the values in the view todo: reflect the height and width from the presentation into the abstraction

            // Bind the position of the abstraction to the values in the view
            newValue.xProperty().bind(root.layoutXProperty());
            newValue.yProperty().bind(root.layoutYProperty());

            // Some debug values in the view todo: remove these at some point
            x.textProperty().bind(newValue.xProperty().asString());
            y.textProperty().bind(newValue.yProperty().asString());

            newValue.getEdges().addListener(new ListChangeListener<Edge>() {
                @Override
                public void onChanged(final Change<? extends Edge> c) {
                    if (c.next()) {
                        // Edges are added to the component
                        c.getAddedSubList().forEach(edge -> {
                            final EdgePresentation edgePresentation = new EdgePresentation(edge, newValue);
                            edgePresentationMap.put(edge, edgePresentation);
                            modelContainer.getChildren().add(edgePresentation);
                        });

                        // Edges are removed from the component
                        c.getRemoved().forEach(edge -> {
                            final EdgePresentation edgePresentation = edgePresentationMap.get(edge);
                            modelContainer.getChildren().remove(edgePresentation);
                            edgePresentationMap.remove(edge);
                        });
                    }
                }
            });
        });

        // The root view have been inflated, initialize the mouse tracker on it
        mouseTracker = new MouseTracker(root);
    }

    public void toggleDeclaration(final MouseEvent mouseEvent) {
        declaration.setVisible(true);

        final Circle circle = new Circle(0);
        circle.setCenterX(component.get().getWidth() - (toggleDeclarationButton.getWidth() - mouseEvent.getX()));
        circle.setCenterY(-1 * mouseEvent.getY());

        final ObjectProperty<Node> clip = new SimpleObjectProperty<>(circle);
        declaration.clipProperty().bind(clip);

        final Transition rippleEffect = new Transition() {
            private final double maxRadius = Math.sqrt(Math.pow(getComponent().getWidth(), 2) + Math.pow(getComponent().getHeight(), 2));

            {
                setCycleDuration(Duration.millis(500));
            }

            protected void interpolate(final double fraction) {
                if (getComponent().isDeclarationOpen()) {
                    circle.setRadius(fraction * maxRadius);
                } else {
                    circle.setRadius(maxRadius - fraction * maxRadius);
                }
                clip.set(circle);
            }
        };

        final Interpolator interpolator = Interpolator.SPLINE(0.785, 0.135, 0.15, 0.86);
        rippleEffect.setInterpolator(interpolator);

        rippleEffect.play();
        getComponent().declarationOpenProperty().set(!getComponent().isDeclarationOpen());
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(final Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    @FXML
    private void modelContainerOnPressed() {
        final Edge unfinishedEdge = getComponent().getUnfinishedEdge();
        if (unfinishedEdge != null) {
            // Calculate the position for the new nail (based on the component position and the canvas mouse tracker)
            final DoubleBinding x = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty());
            final DoubleBinding y = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty());

            // Create the abstraction for the new nail and add it to the unfinished edge
            final Nail newNail = new Nail(x, y);
            unfinishedEdge.addNail(newNail);
        }
    }

    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }
}
