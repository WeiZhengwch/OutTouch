package me.banendy.client.misc;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class RawMouseHelper extends MouseHelper {

    @Override
    public void mouseXYChange()
    {
        if (Minecraft.getMinecraft().gameSettings.rawmouseinput) {
        this.deltaX = RawInputHandler.dx;
        RawInputHandler.dx = 0;
        this.deltaY = -RawInputHandler.dy;
        RawInputHandler.dy = 0;
        } else {
            deltaX = Mouse.getDX();
            deltaY = Mouse.getDY();
        }
    }
    @Override
    public void grabMouseCursor()
    {
        if (Minecraft.getMinecraft().gameSettings.rawmouseinput) {
        Mouse.setGrabbed(true);
        this.deltaX = 0;
        RawInputHandler.dx = 0;
        this.deltaY = 0;
        RawInputHandler.dy = 0;
        } else {
            Mouse.setGrabbed(true);
            deltaX = 0;
            deltaY = 0;
        }
    }
}