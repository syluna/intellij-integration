package com.jmonkeystore.ide.jme.camera;

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

public class EditorCameraState extends BaseAppState implements AnalogListener, ActionListener, RawInputListener {

    private Camera cam;

    private final float[] camAngles = new float[3];
    private final Quaternion camRotation = new Quaternion();

    private float panSpeed = 5f;
    private float rotateSpeed = 5f;
    private float zoomSpeed = 50.0f;

    private boolean rmb_pressed = false;
    private boolean mmb_pressed = false;

    public EditorCameraState() {

    }

    public void setActiveCamera(Camera activeCamera) {
        this.cam = activeCamera;
        this.cam.getRotation().toAngles(camAngles);
    }

    public void removeActiveCamera() {
        this.cam = null;
        this.rmb_pressed = false;
        this.mmb_pressed = false;
    }

    public float getPanSpeed() {
        return panSpeed;
    }

    public void setPanSpeed(float panSpeed) {
        this.panSpeed = panSpeed;
    }

    public float getRotateSpeed() {
        return rotateSpeed;
    }

    public void setRotateSpeed(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    public float getZoomSpeed() {
        return zoomSpeed;
    }

    public void setZoomSpeed(float zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    @Override
    protected void initialize(Application app) {

        InputManager inputManager = getApplication().getInputManager();

        inputManager.addMapping("MouseAxisX", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MouseAxisX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));

        inputManager.addMapping("MouseAxisY", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
                inputManager.addMapping("MouseAxisY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));

        inputManager.addMapping("MouseWheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("MouseWheel-", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        // inputManager.addMapping("MouseButtonLeft", new MouseButtonTrigger(0));
        inputManager.addMapping("MouseButtonMiddle", new MouseButtonTrigger(2));
        inputManager.addMapping("MouseButtonRight", new MouseButtonTrigger(1));

        inputManager.addRawInputListener(this);
        inputManager.addListener(this,
                "MouseAxisX", "MouseAxisY",
                "MouseAxisX-", "MouseAxisY-",
                "MouseWheel", "MouseWheel-",
                /* "MouseButtonLeft", */ "MouseButtonMiddle", "MouseButtonRight");

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }



    // ActionListener
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        if (cam == null) {
            return;
        }

        if (name.equals("MouseButtonRight")) {
            rmb_pressed = isPressed;
        }

        if (name.equals("MouseButtonMiddle")) {
            mmb_pressed = isPressed;
        }

    }

    private void panCamera(float left, float up) {

        Vector3f leftVec = cam.getLeft().mult(left * panSpeed);
        Vector3f upVec = cam.getUp().mult(up * panSpeed);

        Vector3f camLoc = cam.getLocation()
                .add(leftVec)
                .add(upVec);

        cam.setLocation(camLoc);
    }

    private void rotateCamera(float x, float y) {

        x /= FastMath.TWO_PI;
        y /= FastMath.TWO_PI;

        x *= rotateSpeed;
        y *= rotateSpeed;

        camAngles[0] += x;
        camAngles[1] += y;

        // 89 degrees. Avoid the "flip" problem.
        float maxRotX = FastMath.HALF_PI - FastMath.DEG_TO_RAD;

        // limit camera rotation on the X axis.
        if (camAngles[0] < -maxRotX) {
            camAngles[0] = -maxRotX;
        }

        if (camAngles[0] > maxRotX) {
            camAngles[0] = maxRotX;
        }

        // stop the angles from becoming too big on the Y axis.
        if (camAngles[1] > FastMath.TWO_PI) {
            camAngles[1] -= FastMath.TWO_PI;
        } else if (camAngles[1] < -FastMath.TWO_PI) {
            camAngles[1] += FastMath.TWO_PI;
        }

        camRotation.fromAngles(camAngles);
        cam.setRotation(camRotation);
    }

    private void zoomCamera(float amount) {

        amount *= zoomSpeed;

        Vector3f camLoc = cam.getLocation();
        Vector3f movement = cam.getDirection().mult(amount);
        Vector3f newLoc = camLoc.add(movement);

        cam.setLocation(newLoc);
    }

    // AnalogListener
    @Override
    public void onAnalog(String name, float value, float tpf) {

        if (cam == null) {
            return;
        }

        // rmb = rotate
        // mmb = pan

        switch (name) {

            case "MouseAxisX": {

                if (rmb_pressed) {
                    rotateCamera(0, -tpf);
                }

                if (mmb_pressed) {
                    panCamera(tpf, 0);
                }

                break;
            }

            case "MouseAxisX-": {

                if (rmb_pressed) {
                    rotateCamera(0, tpf);
                }

                if (mmb_pressed) {
                    panCamera(-tpf, 0);
                }

                break;
            }


            case "MouseAxisY": {

                if (rmb_pressed) {
                    rotateCamera(-tpf, 0);
                }

                if (mmb_pressed) {
                    panCamera(0, -tpf);
                }

                break;
            }

            case "MouseAxisY-": {

                if (rmb_pressed) {
                    rotateCamera(tpf, 0);
                }

                if (mmb_pressed) {
                    panCamera(0, tpf);
                }

                break;
            }


            case "MouseWheel": {
                zoomCamera(tpf);
                break;
            }

            case "MouseWheel-": {
                zoomCamera(-tpf);
                break;
            }

        }

    }

    // RawInputListener
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

    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {

    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {

    }

    @Override
    public void onTouchEvent(TouchEvent evt) {

    }
}
