package com.genuineminecraft.battletext.events;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

import com.genuineminecraft.battletext.core.Text;

public class BattleTextContainer {

	private static BattleTextContainer instance;

	public static BattleTextContainer getInstance() {
		if (instance == null)
			instance = new BattleTextContainer();
		return instance;
	}

	public long time = 0L;
	public List<Text> textList = Collections.synchronizedList(new ArrayList<Text>());

	public BattleTextContainer() {
	}

	public synchronized void addText(Text txt) {
		if (txt.amount >= 0)
			this.textList.add(txt);
	}

	public synchronized void tick(float deltaTime) {
		if (RenderManager.instance == null || RenderManager.instance.worldObj == null)
			return;
		long tick = RenderManager.instance.worldObj.getTotalWorldTime();
		if (this.time != tick) {
			List<Text> removalQueue = new ArrayList<Text>();
			for (Text caption : textList)
				if (!caption.onUpdate())
					removalQueue.add(caption);
			this.time = tick;
			textList.removeAll(removalQueue);
			removalQueue.clear();
		}
		renderText(deltaTime);
	}

	public void renderText(float delta) {
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		glPushMatrix();
		glDisable(GL_LIGHTING);
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (Text txt : textList) {
			if (txt.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) > 32)
				continue;
			double x = RenderManager.instance.viewerPosX - (txt.prevPosX + ((txt.posX - txt.prevPosX) * delta));
			double y = RenderManager.instance.viewerPosY - (txt.prevPosY + ((txt.posY - txt.prevPosY) * delta)) - 2;
			double z = RenderManager.instance.viewerPosZ - (txt.prevPosZ + ((txt.posZ - txt.prevPosZ) * delta));
			glTranslated(-x, -y, -z);
			glRotatef(-RenderManager.instance.playerViewY + 180, 0.0F, 1.0F, 0.0F);
			glRotatef(-RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
			int alpha1 = (int) (txt.getInterpPercent(delta) * 0xFF) & 0xFF;
			int alpha2 = (int) (txt.getInterpPercent(delta) * 0x7F) & 0xFF;
			if (alpha1 < 28)
				alpha1 = 28;
			if (alpha2 < 28)
				alpha2 = 28;
			int color1 = txt.textColor | (alpha1 << 24);
			int color2 = txt.backgroundColor | (alpha2 << 24);
			int offX = -fr.getStringWidth(txt.display);
			int offY = -4;
			double scale = 0.025;
			glScaled(scale, -scale, scale);
			// Shadows
			fr.drawString(txt.display, offX + 1, offY, color2);
			fr.drawString(txt.display, offX, offY + 1, color2);
			fr.drawString(txt.display, offX, offY - 1, color2);
			fr.drawString(txt.display, offX - 1, offY, color2);
			// Main
			fr.drawString(txt.display, offX, offY, color1);
			glScaled(1.0 / scale, -1.0 / scale, 1.0 / scale);
			glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
			glRotatef(RenderManager.instance.playerViewY - 180, 0.0F, 1.0F, 0.0F);
			glTranslated(x, y, z);
		}
		glDisable(GL_BLEND);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);
		glPopMatrix();
	}
}
