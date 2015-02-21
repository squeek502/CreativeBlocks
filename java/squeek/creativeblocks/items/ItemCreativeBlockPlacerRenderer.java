package squeek.creativeblocks.items;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import squeek.creativeblocks.CreativeBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemCreativeBlockPlacerRenderer implements IItemRenderer
{
	// copied from ItemRenderer
	public static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	@Override
	public boolean handleRenderType(ItemStack itemStack, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack itemStack, ItemRendererHelper helper)
	{
		if (type == ItemRenderType.EQUIPPED && helper == ItemRendererHelper.BLOCK_3D)
		{
			ItemCreativeBlockPlacer placerItem = (ItemCreativeBlockPlacer) itemStack.getItem();
			boolean hasBlock = placerItem.hasBlock(itemStack);
			ItemStack blockToRender = hasBlock ? placerItem.getBlock(itemStack) : new ItemStack(Blocks.stone);
			Item blockItem = blockToRender.getItem();
			Block block = Block.getBlockFromItem(blockItem);
			boolean rendersIn3d = blockToRender.getItemSpriteNumber() == 0 && blockItem instanceof ItemBlock && RenderBlocks.renderItemIn3d(block.getRenderType());
			return rendersIn3d;
		}
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data)
	{
		if (itemStack.getItem() != CreativeBlocks.creativeBlockPlacer)
			return;

		boolean isRenderingEquipped = type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.EQUIPPED;
		ItemCreativeBlockPlacer placerItem = (ItemCreativeBlockPlacer) itemStack.getItem();
		boolean hasBlock = placerItem.hasBlock(itemStack);
		ItemStack blockToRender = hasBlock ? placerItem.getBlock(itemStack) : new ItemStack(Blocks.stone);
		Item blockItem = blockToRender.getItem();
		Block block = Block.getBlockFromItem(blockItem);
		boolean rendersIn3d = blockToRender.getItemSpriteNumber() == 0 && blockItem instanceof ItemBlock && RenderBlocks.renderItemIn3d(block.getRenderType());

		GL11.glPushMatrix();

		switch (type)
		{
			case ENTITY:
				if (rendersIn3d)
					GL11.glScalef(0.5F, 0.5F, 0.5F);
				else
					GL11.glTranslatef(0F, 0.25F, 0F);
				break;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
				GL11.glTranslatef(0.5F, 0.5F, 0.5F);
				break;
			case INVENTORY:
			default:
				break;
		}

		if (!hasBlock || !isRenderingEquipped)
		{
			GL11.glRotatef((float) (System.currentTimeMillis() % 36000) / 10F, 0F, 1F, 0F);
		}
		if (!rendersIn3d && !isRenderingEquipped)
		{
			GL11.glTranslatef(-0.5F, -0.5F, 0F);
		}

		int prevBlendSrc = 0, prevBlendDst = 0;
		if (!hasBlock)
		{
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			prevBlendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
			prevBlendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
			GL11.glBlendFunc(GL11.GL_CONSTANT_COLOR, GL11.GL_CONSTANT_COLOR);
		}

		EntityLivingBase entityLiving = getEntityLivingFromRenderType(type, data);
		for (int pass = 0; pass < blockToRender.getItem().getRenderPasses(blockToRender.getItemDamage()); pass++)
		{
			int k1 = blockToRender.getItem().getColorFromItemStack(blockToRender, pass);
			float r = (float) (k1 >> 16 & 255) / 255.0F;
			float g = (float) (k1 >> 8 & 255) / 255.0F;
			float b = (float) (k1 & 255) / 255.0F;
			GL11.glColor4f(r, g, b, 1.0F);

			if (rendersIn3d || isRenderingEquipped)
				RenderManager.instance.itemRenderer.renderItem(entityLiving, blockToRender, pass, type);
			else
				renderUntranslatedItemIn2D(entityLiving, blockToRender, pass, type);
		}

		if (!hasBlock)
		{
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glBlendFunc(prevBlendSrc, prevBlendDst);
		}

		GL11.glPopMatrix();
	}

	public static void renderUntranslatedItemIn2D(EntityLivingBase entityLiving, ItemStack itemStack, int pass, ItemRenderType type)
	{
		IIcon iicon = entityLiving.getItemIcon(itemStack, pass);

		if (itemStack == null || iicon == null)
		{
			return;
		}

		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		textureManager.bindTexture(textureManager.getResourceLocation(itemStack.getItemSpriteNumber()));
		TextureUtil.func_152777_a(false, false, 1.0F);
		Tessellator tessellator = Tessellator.instance;
		float f = iicon.getMinU();
		float f1 = iicon.getMaxU();
		float f2 = iicon.getMinV();
		float f3 = iicon.getMaxV();
		ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);

		if (itemStack.hasEffect(pass))
		{
			GL11.glDepthFunc(GL11.GL_EQUAL);
			GL11.glDisable(GL11.GL_LIGHTING);
			textureManager.bindTexture(RES_ITEM_GLINT);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(768, 1, 1, 0);
			float f7 = 0.76F;
			GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glPushMatrix();
			float f8 = 0.125F;
			GL11.glScalef(f8, f8, f8);
			float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
			GL11.glTranslatef(f9, 0.0F, 0.0F);
			GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
			ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glScalef(f8, f8, f8);
			f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
			GL11.glTranslatef(-f9, 0.0F, 0.0F);
			GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
			ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
		}

		textureManager.bindTexture(textureManager.getResourceLocation(itemStack.getItemSpriteNumber()));
		TextureUtil.func_147945_b();

		GL11.glDisable(GL11.GL_BLEND);

		GL11.glPopMatrix();
	}

	public static EntityLivingBase getEntityLivingFromRenderType(ItemRenderType type, Object... data)
	{
		EntityPlayer localPlayer = Minecraft.getMinecraft().thePlayer;
		switch (type)
		{
			case ENTITY:
			case INVENTORY:
				return localPlayer;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
				return (EntityLivingBase) data[1];
			case FIRST_PERSON_MAP:
				return (EntityPlayer) data[0];
			default:
				return null;
		}
	}
}
