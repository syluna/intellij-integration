package com.jmonkeystore.ide.jme.camera;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.*;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jmonkeystore.ide.jme.JmeEngineService;

/**
 * @author Normen Hanssen
 * @author James Khan
 */
public class SceneCameraState extends BaseAppState implements ActionListener, AnalogListener, RawInputListener {

    private Camera cam;
    private Quaternion rot = new Quaternion();
    private Vector3f focus = new Vector3f();
    private Vector3f vector = new Vector3f(0, 0, 5);

    private int mouseX = 0;
    private int mouseY = 0;

    private boolean moved = false;
    private boolean movedR = false;

    private boolean buttonDownL = false;
    private boolean buttonDownR = false;
    private boolean buttonDownM = false;

    private boolean shiftModifier = false;

    private boolean checkClickL = false;
    private boolean checkClickR = false;
    private boolean checkClickM = false;
    private boolean checkReleaseL = false;
    private boolean checkReleaseR = false;
    private boolean checkReleaseM = false;

    public SceneCameraState() {
        // this.engineService = ServiceManager.getService(JmeEngineService.class);
    }

    public void setCamera(Camera camera) {
        this.cam = camera;
    }

    public void removeActiveCamera() {
        this.cam = null;
    }

    /*
     * methods to move camera
     */
    private void doRotateCamera(Vector3f axis, float amount) {
        if (axis.equals(cam.getLeft())) {
            float elevation = -FastMath.asin(cam.getDirection().y);
            amount = Math.min(Math.max(elevation + amount,
                    -FastMath.HALF_PI), FastMath.HALF_PI)
                    - elevation;
        }
        rot.fromAngleAxis(amount, axis);
        cam.getLocation().subtract(focus, vector);
        rot.mult(vector, vector);
        focus.add(vector, cam.getLocation());

        Quaternion curRot = cam.getRotation().clone();
        cam.setRotation(rot.mult(curRot));

        /*
        java.awt.EventQueue.invokeLater(new Runnable() {


            public void run() {

                SceneViewerTopComponent svtc = SceneViewerTopComponent.findInstance();
                if (svtc != null) {
                    CameraToolbar.getInstance().switchToView(View.User);
                }

            }

        });

         */
    }

    private void doPanCamera(float left, float up) {
        cam.getLeft().mult(left, vector);
        vector.scaleAdd(up, cam.getUp(), vector);
        vector.multLocal(cam.getLocation().distance(focus));
        cam.setLocation(cam.getLocation().add(vector));
        focus.addLocal(vector);
    }

    private void doMoveCamera(float forward) {
        cam.getDirection().mult(forward, vector);
        cam.setLocation(cam.getLocation().add(vector));
    }

    private void doZoomCamera(float amount) {
        amount = cam.getLocation().distance(focus) * amount;
        float dist = cam.getLocation().distance(focus);
        amount = dist - Math.max(0f, dist - amount);
        Vector3f loc = cam.getLocation().clone();
        loc.scaleAdd(amount, cam.getDirection(), loc);
        cam.setLocation(loc);

        if (cam.isParallelProjection()) {
            float aspect = (float) cam.getWidth() / cam.getHeight();
            float h = FastMath.tan(45f * FastMath.DEG_TO_RAD * .5f) * dist;
            float w = h * aspect;
            cam.setFrustum(-1000, 1000, -w, w, h, -h);
        }

    }

    public boolean doToggleOrthoPerspMode() {

        float aspect = (float) cam.getWidth() / cam.getHeight();
        if (!cam.isParallelProjection()) {
            cam.setParallelProjection(true);
            float h = cam.getFrustumTop();
            float w;
            float dist = cam.getLocation().distance(focus);
            float fovY = FastMath.atan(h) / (FastMath.DEG_TO_RAD * .5f);
            h = FastMath.tan(fovY * FastMath.DEG_TO_RAD * .5f) * dist;
            w = h * aspect;
            cam.setFrustum(-1000, 1000, -w, w, h, -h);
            return true;
        } else {
            cam.setParallelProjection(false);
            cam.setFrustumPerspective(45f, aspect, 1, 1000);
            return false;
        }
    }

    /*
    public void toggleOrthoPerspMode() {
        try {

            CameraToolbar.getInstance().toggleOrthoMode(SceneApplication.getApplication().enqueue(new Callable<Boolean>() {

                public Boolean call() throws Exception {
                    return doToggleOrthoPerspMode();
                }
            }).get());
        } catch (InterruptedException | ExecutionException ex) {
            // Exceptions.printStackTrace(ex);
            ex.printStackTrace();
        }
    }

     */

    public void switchToView(final CameraView view) {

        ServiceManager.getService(JmeEngineService.class).enqueue(() -> {

                float dist = cam.getLocation().distance(focus);
                switch (view) {
                    case Front:
                        cam.setLocation(new Vector3f(focus.x, focus.y, focus.z + dist));
                        cam.lookAt(focus, Vector3f.UNIT_Y);
                        break;
                    case Left:
                        cam.setLocation(new Vector3f(focus.x + dist, focus.y, focus.z));
                        cam.lookAt(focus, Vector3f.UNIT_Y);
                        break;
                    case Right:
                        cam.setLocation(new Vector3f(focus.x - dist, focus.y, focus.z));
                        cam.lookAt(focus, Vector3f.UNIT_Y);
                        break;
                    case Back:
                        cam.setLocation(new Vector3f(focus.x, focus.y, focus.z - dist));
                        cam.lookAt(focus, Vector3f.UNIT_Y);
                        break;
                    case Top:
                        cam.setLocation(new Vector3f(focus.x, focus.y + dist, focus.z));
                        cam.lookAt(focus, Vector3f.UNIT_Z.mult(-1));

                        break;
                    case Bottom:
                        cam.setLocation(new Vector3f(focus.x, focus.y - dist, focus.z));
                        cam.lookAt(focus, Vector3f.UNIT_Z);
                        break;
                    case User:
                    default:
                }
        });

        // CameraToolbar.getInstance().switchToView(view);

    }

    @Override
    protected void initialize(Application app) {

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

        InputManager inputManager = getApplication().getInputManager();

        inputManager.addMapping("MouseAxisX", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MouseAxisY", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("MouseAxisX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MouseAxisY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("MouseWheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("MouseWheel-", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("MouseButtonLeft", new MouseButtonTrigger(0));
        inputManager.addMapping("MouseButtonMiddle", new MouseButtonTrigger(2));
        inputManager.addMapping("MouseButtonRight", new MouseButtonTrigger(1));

        inputManager.addRawInputListener(this);
        inputManager.addListener(this,
                "MouseAxisX", "MouseAxisY",
                "MouseAxisX-", "MouseAxisY-",
                "MouseWheel", "MouseWheel-",
                "MouseButtonLeft", "MouseButtonMiddle", "MouseButtonRight");
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void beginInput() {

    }

    @Override
    public void endInput() {

    }

    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {

    }

    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {

    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {
        mouseX = evt.getX();
        mouseY = evt.getY();
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {
        /*
        //on a click release we request the focus for the top component
        //this allow netbeans to catch keyEvents and trigger actions according to keymapping
        if ("true".equals(NbPreferences.forModule(Installer.class).get("use_lwjgl_canvas", "false"))) {
            if (mbe.isReleased()) {
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        SceneViewerTopComponent.findInstance().requestActive();
                    }
                });
            }
        }

         */
    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {

    }

    @Override
    public void onTouchEvent(TouchEvent evt) {

    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        if (cam == null) {
            return;
        }

        if ("MouseButtonLeft".equals(name)) {


            if (isPressed) {
                if (!buttonDownL) { // mouse clicked
                    checkClickL = true;
                    checkReleaseL = false;
                }
            } else {
                if (buttonDownL) { // mouse released
                    checkReleaseL = true;
                    checkClickL = false;
                }
            }
            buttonDownL = isPressed;
        }
        if ("MouseButtonRight".equals(name)) {
            if (isPressed) {
                if (!buttonDownR) { // mouse clicked
                    checkClickR = true;
                    checkReleaseR = false;
                }
            } else {
                if (buttonDownR) { // mouse released
                    checkReleaseR = true;
                    checkClickR = false;
                }
            }
            buttonDownR = isPressed;
        }
        if ("MouseButtonMiddle".equals(name)) {

            if (isPressed) {
                if (!buttonDownM) { // mouse clicked
                    checkClickM = true;
                    checkReleaseM = false;
                }
            } else {
                if (buttonDownM) { // mouse released
                    checkReleaseM = true;
                    checkClickM = false;
                }
            }
            buttonDownM = isPressed;
        }

    }

    public boolean useCameraControls() {
        return true;
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {

        if (cam == null) {
            return;
        }

        if ("MouseAxisX".equals(name)) {
            moved = true;
            movedR = true;

            if ((buttonDownL && useCameraControls()) || (buttonDownM && !shiftModifier)) {
                doRotateCamera(Vector3f.UNIT_Y, -value * 2.5f);
            }
            if ((buttonDownR && useCameraControls()) || (buttonDownM && shiftModifier)) {
                doPanCamera(value * 2.5f, 0);
            }

        } else if ("MouseAxisY".equals(name)) {
            moved = true;
            movedR = true;

            if ((buttonDownL && useCameraControls()) || (buttonDownM && !shiftModifier)) {
                doRotateCamera(cam.getLeft(), -value * 2.5f);
            }
            if ((buttonDownR && useCameraControls()) || (buttonDownM && shiftModifier)) {
                doPanCamera(0, -value * 2.5f);
            }

        } else if ("MouseAxisX-".equals(name)) {
            moved = true;
            movedR = true;

            if ((buttonDownL && useCameraControls()) || (buttonDownM && !shiftModifier)) {
                doRotateCamera(Vector3f.UNIT_Y, value * 2.5f);
            }
            if ((buttonDownR && useCameraControls()) || (buttonDownM && shiftModifier)) {
                doPanCamera(-value * 2.5f, 0);
            }

        } else if ("MouseAxisY-".equals(name)) {
            moved = true;
            movedR = true;

            if ((buttonDownL && useCameraControls()) || (buttonDownM && !shiftModifier)) {
                doRotateCamera(cam.getLeft(), value * 2.5f);
            }
            if ((buttonDownR && useCameraControls()) || (buttonDownM && shiftModifier)) {
                doPanCamera(0, value * 2.5f);
            }

        } else if ("MouseWheel".equals(name)) {
            doZoomCamera(.1f);
        } else if ("MouseWheel-".equals(name)) {
            doZoomCamera(-.1f);
        }

    }

    /**
     * mouse clicked, not dragged
     * @param button
     * @param pressed true if pressed, false if released
     */
    private void checkClick(int button, boolean pressed) {
    }

    /**
     * Mouse dragged while button is depressed
     * @param button
     * @param pressed
     */
    private void checkDragged(int button, boolean pressed) {
    }

    /**
     * The mouse moved, no dragging or buttons pressed
     */
    private void checkMoved() {
        // override in subclasses
    }

    @Override
    public void update(float f) {

        if (cam == null) {
            return;
        }

        if (moved) {
            // moved, check for drags
            if (checkReleaseL || checkReleaseR || checkReleaseM) {
                // drag released
                if (checkReleaseL) {
                    checkDragged(0, false);
                }
                if (checkReleaseR) {
                    checkDragged(1, false);
                }
                if (checkReleaseM) {
                    checkDragged(2, false);
                }
                checkReleaseL = false;
                checkReleaseR = false;
                checkReleaseM = false;
            } else {
                if (buttonDownL) {
                    checkDragged(0, true);
                } else if (buttonDownR) {
                    checkDragged(1, true);
                } else if (buttonDownM) {
                    checkDragged(2, true);
                } else {
                    checkMoved(); // no dragging, just moved
                }
            }

            moved = false;
        } else {
            // not moved, check for just clicks
            if (checkClickL) {
                checkClick(0, true);
                checkClickL = false;
            }
            if (checkReleaseL) {
                checkClick(0, false);
                checkReleaseL = false;
            }
            if (checkClickR) {
                checkClick(1, true);
                checkClickR = false;
            }
            if (checkReleaseR) {
                checkClick(1, false);
                checkReleaseR = false;
            }
            if (checkClickM) {
                checkClick(2, true);
                checkClickM = false;
            }
            if (checkReleaseM) {
                checkClick(2, false);
                checkReleaseM = false;
            }
        }

        /*
         * if (checkDragged || checkDraggedR) { if (checkDragged) {
         * checkDragged(0); checkReleaseLeft = false; checkDragged = false;
         * checkClick = false; checkClickR = false; } if (checkDraggedR) {
         * checkDragged(1); checkReleaseRight = false; checkDraggedR = false;
         * checkClick = false; checkClickR = false; } } else { if (checkClick) {
         * checkClick(0, checkReleaseLeft); checkReleaseLeft = false; checkClick
         * = false; checkDragged = false; checkDraggedR = false; } if
         * (checkClickR) { checkClick(1, checkReleaseRight); checkReleaseRight =
         * false; checkClickR = false; checkDragged = false; checkDraggedR =
         * false; } }
         */
    }

}
