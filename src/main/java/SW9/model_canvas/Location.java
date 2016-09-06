package SW9.model_canvas;

import SW9.MouseTracker;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Location extends Circle {

    private final static double RADIUS = 25.0f;

    private boolean isOnMouse = true;
    private final MouseTracker parentMouseTracker;
    public final MouseTracker localMouseTracker = new MouseTracker();

    public Location(MouseTracker parentMouseTracker) {
        this(parentMouseTracker.getX(), parentMouseTracker.getY(), parentMouseTracker);
    }

    public Location(final double centerX, final double centerY, final MouseTracker parentMouseTracker) {
        super(centerX, centerY, RADIUS);
        this.parentMouseTracker = parentMouseTracker;

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
                    System.out.println("aids");
                });
            }
        };

        // Register the handler for placing the location
        localMouseTracker.registerOnMouseClickedEventHandler(placeAtMouseHandler);

        // Register the handler for dragging of the location (is unregistered when clicked)
        parentMouseTracker.registerOnMouseMovedEventHandler(followMouseHandler);
    }


}
