package net.braunly.ponymagic.gui;

import com.google.common.collect.ImmutableList;
import me.braunly.ponymagic.api.PonyMagicAPI;
import me.braunly.ponymagic.api.enums.EnumRace;
import me.braunly.ponymagic.api.interfaces.IPlayerDataStorage;
import net.braunly.ponymagic.PonyMagic;
import net.braunly.ponymagic.client.KeyBindings;
import net.braunly.ponymagic.config.SkillConfig;
import net.braunly.ponymagic.network.packets.RequestPlayerDataPacket;
import net.braunly.ponymagic.network.packets.ResetPacket;
import net.braunly.ponymagic.network.packets.SkillUpPacket;
import net.braunly.ponymagic.skill.Skill;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;

public class GuiSkills extends GuiScreen {

	private final ResourceLocation bg = new ResourceLocation(PonyMagic.MODID, "textures/gui/skills_bg.png");
	private final ResourceLocation expBar = new ResourceLocation(PonyMagic.MODID, "textures/gui/exp_bar.png");
	private final ResourceLocation lvlUp = new ResourceLocation(PonyMagic.MODID, "textures/gui/lvl_up.png");
	private final ResourceLocation skillActive = new ResourceLocation(PonyMagic.MODID, "textures/gui/skill_active.png");
	private final ResourceLocation skillAvailable = new ResourceLocation(PonyMagic.MODID,
			"textures/gui/skill_available.png");
	private final ResourceLocation skillLearned = new ResourceLocation(PonyMagic.MODID,
			"textures/gui/skill_learned.png");
	private final ResourceLocation skillUnAvailable = new ResourceLocation(PonyMagic.MODID,
			"textures/gui/skill_unavailable.png");
	// Lines
	private final ResourceLocation line_uub = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_uub.png");
	private final ResourceLocation line_uug = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_uug.png");
	private final ResourceLocation line_ub = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_ub.png");
	private final ResourceLocation line_ug = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_ug.png");
	private final ResourceLocation line_hb = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_hb.png");
	private final ResourceLocation line_hg = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_hg.png");
	private final ResourceLocation line_db = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_db.png");
	private final ResourceLocation line_dg = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_dg.png");
	private final ResourceLocation line_ddb = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_ddb.png");
	private final ResourceLocation line_ddg = new ResourceLocation(PonyMagic.MODID, "textures/gui/line_ddg.png");

	private IPlayerDataStorage playerData = null;
	private Set<GuiButtonSkill> skillsNet = null;
	private GuiButtonSkill skillClicked = null;

	@Override
	public void initGui() {
		// FIXME ?
		initPlayerData();

		// FIXME: on first open not showing skills
		if (this.playerData != null && this.playerData.getRace() != EnumRace.REGULAR) {
			// Init skills net
			this.skillsNet = GuiSkillsNet.getInstance().getSkillNet(this.playerData.getRace());
			// Needs for actionPerformed function
			this.buttonList.addAll(this.skillsNet);
//			PonyMagic.log.info("[GUI] Skillnet inited!");
		} else {
			this.mc.displayGuiScreen(null);
		}

	}

	private void initPlayerData() {
		PonyMagic.channel.sendToServer(new RequestPlayerDataPacket());
		this.playerData = PonyMagicAPI.getPlayerDataStorage(this.mc.player);
//		PonyMagic.log.info("[GUI] Player data inited!");
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		GuiButtonSkill skill = (GuiButtonSkill) button;
//		PonyMagic.log.info("[GUI] Action - " + skill.skillName);
		if (this.skillClicked == skill) {
			processButton(skill);
		} else {
			this.skillClicked = skill;
		}
	}

	private void processButton(GuiButtonSkill skill) {
		if (skill.skillName.equals("reset")) {
//			PonyMagic.log.info("[GUI] Reset");
			PonyMagic.channel.sendToServer(new ResetPacket());
			initPlayerData();
			return;
		}

		// :FIXME: Move to SERVER side
		if (!isSkillLearned(skill) && isSkillAvailable(skill)) {
//			PonyMagic.log.info("[GUI] Send skillUp");
			PonyMagic.channel.sendToServer(new SkillUpPacket(skill.skillName, skill.skillLevel));
			initPlayerData();
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {      
        if (keyCode == KeyBindings.skills_gui.getKeyCode()) {
        	this.mc.displayGuiScreen(null);
        }
        super.keyTyped(typedChar, keyCode);
    }

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//		PonyMagic.log.info("[GUI] Start draw...");
		this.drawDefaultBackground();

		// Get data for rendering
		int playerLevel = this.playerData.getLevelData().getLevel();
		int playerFreeSkillPoints = this.playerData.getLevelData().getFreeSkillPoints();
		EnumRace playerRace = this.playerData.getRace();

		// Background
		int w = 496;
		int h = 334;

		float scale = Math.min(Math.min((float)(width) / w, (float)(height) / h), 1.0F);
		int x = (this.width - (int)(w * scale)) / 2;
		int y = (this.height - (int)(h * scale)) / 2;

		GlStateManager.pushMatrix();
		GlStateManager.scale(scale, scale, scale);

//		PonyMagic.log.info("[GUI] Draw BG");
		this.mc.getTextureManager().bindTexture(this.bg);
		drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, 512, 512); // 496.334

		GlStateManager.popMatrix();

		// Draw player level and free points
		GlStateManager.pushMatrix();
		GlStateManager.scale(1.1F, 1.1F, 1.1F);  // looks nicer
		drawCenteredString(this.fontRenderer,
				new TextComponentTranslation("gui.skills.level", playerLevel).getFormattedText()
					+ "                    "
					+ new TextComponentTranslation("gui.skills.freeskillpoints", playerFreeSkillPoints).getFormattedText(),
				(int)((x + 250) * scale / 1.1F), (int)((y + 295) * scale / 1.1F), 16773290);  // y + 300 caused it to overlap with exp bar on smaller scales
		GlStateManager.color(1, 1, 1, 1);  // icons shadow fix
		GlStateManager.popMatrix();
		
		try {
//			PonyMagic.log.info("[GUI] Draw skills");
			// Draw skills
			if (playerRace != null && playerRace != EnumRace.REGULAR && this.skillsNet != null) {
				// TODO: rewrite loops with this.zIndex

				// Draw skills lines
				for (GuiButtonSkill skill : this.skillsNet) {

					// Skill button init
					skill.initButton(this.mc, x, y, scale);

					// Lines
					if (!skill.lines.isEmpty()) {
						for (String itLines : skill.lines) {
							boolean lineActive = false;
							GuiButtonSkill lineSkill = GuiSkillsNet.getInstance().getRaceSkill(playerRace, itLines);
							if (this.isSkillLearned(skill) && this.isSkillLearned(lineSkill)) {
								lineActive = true;
							}

							int lineDirection = lineSkill.posY - skill.posY;

							GlStateManager.pushMatrix();
							GlStateManager.scale(scale, scale, scale);
							switch (lineDirection) {
							case -64:
								this.mc.getTextureManager().bindTexture(lineActive ? this.line_uug : this.line_uub);
								drawModalRectWithCustomSizedTexture(skill.posX + 32, skill.posY - 48, 0, 0, 32, 64, 32, 64);
								break;
							case -32:
								this.mc.getTextureManager().bindTexture(lineActive ? this.line_ug : this.line_ub);
								drawModalRectWithCustomSizedTexture(skill.posX + 32, skill.posY - 16, 0, 0, 32, 32, 32, 32);
								break;
							case 0:
								this.mc.getTextureManager().bindTexture(lineActive ? this.line_hg : this.line_hb);
								drawModalRectWithCustomSizedTexture(skill.posX + 16, skill.posY, 0, 0, 64, 32, 64, 32);
								break;
							case 32:
								this.mc.getTextureManager().bindTexture(lineActive ? this.line_dg : this.line_db);
								drawModalRectWithCustomSizedTexture(skill.posX + 32, skill.posY + 16, 0, 0, 32, 32, 32, 32);
								break;
							case 64:
								this.mc.getTextureManager().bindTexture(lineActive ? this.line_ddg : this.line_ddb);
								drawModalRectWithCustomSizedTexture(skill.posX + 32, skill.posY + 16, 0, 0, 32, 64, 32, 64);
								break;
							default:
								break;
							}
							GlStateManager.popMatrix();
						}
					}
				}

				// Draw skills background
				for (GuiButtonSkill skill : this.skillsNet) {
					// Skill background
					if (this.isSkillLearned(skill)) {
						this.mc.getTextureManager().bindTexture(this.skillLearned);
					} else if (this.skillClicked == skill) {
						this.mc.getTextureManager().bindTexture(this.skillActive);
					} else if (this.isSkillAvailable(skill)) {
						this.mc.getTextureManager().bindTexture(this.skillAvailable);
					} else {
						this.mc.getTextureManager().bindTexture(this.skillUnAvailable);
					}
					GlStateManager.pushMatrix();
					GlStateManager.scale(scale, scale, scale);
					drawModalRectWithCustomSizedTexture(skill.posX - 2, skill.posY - 2, 0, 0, 36, 36, 36, 36);
					GlStateManager.popMatrix();

					// Draw icon
					skill.drawButton();
				}

				// Showing info for skills
				for (GuiButtonSkill skill : this.skillsNet) {
					if (skill.isUnderMouse(mouseX, mouseY)) {
						Skill skillConfig = SkillConfig.getRaceSkill(playerRace, skill.skillName, skill.skillLevel);
						// Skill name and description
						ImmutableList.Builder<String> skillHoverText = new ImmutableList.Builder<String>()
								.add(playerRace.getColor() + new TextComponentTranslation("skill." + skill.skillName + skill.skillLevel + ".name").getFormattedText())
								.add(new TextComponentTranslation("skill." + skill.skillName + skill.skillLevel + ".descr").getFormattedText());
						// Skill details
						if (skillConfig != null) {
							if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
								Map<String, Integer> skillEffect = skillConfig.getEffect();
								skillHoverText
										.add(new TextComponentTranslation(
												"gui.skills.usage",
												new TextComponentTranslation("skill." + skill.skillName + skill.skillLevel + ".command").getFormattedText()
										).getFormattedText())
										.add(new TextComponentTranslation(
												"gui.skills.price",
												skillConfig.getPrice()
										).getFormattedText())
										.add(new TextComponentTranslation(
												"gui.skills.stamina",
												skillConfig.getStamina()
										).getFormattedText());
								if (!skillEffect.isEmpty()) {
									skillHoverText
											.add(new TextComponentTranslation(
													"gui.skills.effect.duration",
													skillEffect.getOrDefault("duration", 0) / 20
											).getFormattedText())
											.add(new TextComponentTranslation(
													"gui.skills.effect.level",
													skillEffect.getOrDefault("level", 0) + 1
											).getFormattedText());
								}
							} else {
								skillHoverText.add(
										TextFormatting.DARK_PURPLE + (TextFormatting.ITALIC + new TextComponentTranslation("gui.skills.hover_text").getFormattedText())
								);
							}
						}

						// Draw skill info text
						RenderHelper.enableStandardItemLighting();
						drawHoveringText(skillHoverText.build(), mouseX, mouseY);
						RenderHelper.disableStandardItemLighting();
					}
				}
			}
		} catch (NullPointerException exc) {
			PonyMagic.log.info("[GUI] ERROR - NullPointerException");
			exc.printStackTrace();
			this.mc.displayGuiScreen(null);
		}
	}

	private boolean isSkillLearned(GuiButtonSkill skill) {
		return this.playerData.getSkillData().getSkillLevel(skill.skillName) >= skill.skillLevel;
	}

	private boolean isSkillAvailable(GuiButtonSkill skill) {
		Skill skillConfig = SkillConfig.getRaceSkill(
				this.playerData.getRace(),
				skill.skillName,
				skill.skillLevel
		);
		if (skillConfig == null) {
			PonyMagic.log.error("Skill config for {}#{} not found!", skill.skillName, skill.skillLevel);
		}
		// Check for free skill points and learned dependencies
		return this.playerData.getLevelData().getFreeSkillPoints() >= skillConfig.getPrice() &&
				this.playerData.getSkillData().isAnySkillLearned(skillConfig.getDepends());

	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
