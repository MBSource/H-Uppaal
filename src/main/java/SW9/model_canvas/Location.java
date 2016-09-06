package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Location extends Circle {

    private final static double RADIUS = 25.0f;

    private boolean isOnMouse = true;
    public final MouseTracker localMouseTracker = new MouseTracker();

    public Location(MouseTracker parentMouseTracker) {
        this(parentMouseTracker.getX(), parentMouseTracker.getY(), parentMouseTracker);
    }

    public Location(final double centerX, final double centerY, final MouseTracker parentMouseTracker) {
        super(centerX, centerY, RADIUS);

        // Initialize the local mouse tracker
        this.setOnMouseMoved(localMouseTracker.onMouseMovedEventHandler);
        this.setOnMouseClicked(localMouseTracker.onMouseClickedEventHandler);

        // Add style
        this.getStyleClass().add("location");

        // Update the position of the new location when the mouse moved
        final EventHandler<MouseEvent> followMouseHandler = mouseMovedEvent -> {
            Location.this.setCenterX(mouseMovedEvent.getX());
            Location.this.setCenterY(mouseMovedEvent.getY());
        };

        // Place the new location when the mouse is pressed (i.e. stop moving it)
        final EventHandler<MouseEvent> placeAtMouseHandler = mouseClickedEvent -> {
            if (isOnMouse) {
                parentMouseTracker.unregisterOnMouseMovedEventHandler(followMouseHandler);

                // Tell the canvas that the mouse is no longer occupied
                ModelCanvas.mouseHasLocation = false;

                Animation locationPlaceAnimation = new Transition() {
                    {
                        setCycleDuration(Duration.millis(50));
                    }

                    protected void interpolate(double frac) {
                        Location.this.setEffect(DropShadowHelper.generateElevationShadow(12 - 12 * frac));
                    }
                };

                locationPlaceAnimation.play();

                locationPlaceAnimation.setOnFinished(event -> {
                    isOnMouse = false;
                });
            } else if (mouseClickedEvent.isShiftDown()) {

                final Edge edge = new Edge(this, parentMouseTracker);

                // Type cast the parent to be the anchor pane and disregard the safety and simple add the edge
                ((Pane) this.getParent()).getChildren().add(edge);

                // Notify the canvas that we are creating an edge
                ModelCanvas.mouseHasEdge = true;

                final Keybind escKeybind[] = { null };
                escKeybind[0] = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                    // Notify the canvas that we not longer are creating an edge
                    ModelCanvas.mouseHasEdge = false;

                    ((Pane) this.getParent()).getChildren().remove(edge);

                    KeyboardTracker.unregisterKeybind(escKeybind[0]);
                });
                KeyboardTracker.registerKeybind(escKeybind[0]);
            }
        };

        // Register the handler for placing the location
        localMouseTracker.registerOnMouseClickedEventHandler(placeAtMouseHandler);

        // Register the handler for dragging of the location (is unregistered when clicked)
        parentMouseTracker.registerOnMouseMovedEventHandler(followMouseHandler);
    }


}
