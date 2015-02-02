package squeek.creativeblocks;

import java.util.Map;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class ASMPlugin implements IFMLLoadingPlugin, IClassTransformer
{
	public static boolean isObfuscated = false;

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{this.getClass().getName()};
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		isObfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (transformedName.equals("net.minecraft.server.management.ItemInWorldManager"))
		{
			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode method;
			AbstractInsnNode targetInsn;

			method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "activateBlockOrUseItem", isObfuscated ? "(Lyz;Lahb;Ladd;IIIIFFF)Z" : "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIIIFFF)Z");
			if (method == null)
				throw new RuntimeException("Couldn't find ItemInWorldManager.activateBlockOrUseItem");
			targetInsn = findTargetInsn(method, classNode.name, isObfuscated ? "d" : "isCreative", "()Z");
			if (targetInsn == null)
				throw new RuntimeException("Couldn't find target instruction in ItemInWorldManager.activateBlockOrUseItem");
			transformIfCreativeBlock(method, targetInsn, getItemStackCheckInsns());

			method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "onBlockClicked", "(IIII)V");
			if (method == null)
				throw new RuntimeException("Couldn't find ItemInWorldManager.onBlockClicked");
			targetInsn = findTargetInsn(method, classNode.name, isObfuscated ? "d" : "isCreative", "()Z");
			if (targetInsn == null)
				throw new RuntimeException("Couldn't find target instruction in ItemInWorldManager.onBlockClicked");
			transformIfCreativeBlock(method, targetInsn, getServerCoordinatesCheckInsns(classNode));

			method = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "tryHarvestBlock", "(III)Z");
			if (method == null)
				throw new RuntimeException("Couldn't find ItemInWorldManager.tryHarvestBlock");
			targetInsn = findTargetInsn(method, classNode.name, isObfuscated ? "d" : "isCreative", "()Z");
			if (targetInsn == null)
				throw new RuntimeException("Couldn't find target instruction in ItemInWorldManager.tryHarvestBlock");
			transformIfCreativeBlock(method, targetInsn, getServerCoordinatesCheckInsns(classNode));

			return writeClassToBytes(classNode);
		}
		else if (transformedName.equals("net.minecraft.client.multiplayer.PlayerControllerMP"))
		{
			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode method;
			AbstractInsnNode targetInsn;

			method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "onPlayerRightClick", isObfuscated ? "(Lyz;Lahb;Ladd;IIIILazw;)Z" : "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIIILnet/minecraft/util/Vec3;)Z");
			if (method == null)
				throw new RuntimeException("Couldn't find PlayerControllerMP.onPlayerRightClick");
			targetInsn = findTargetInsn(method, isObfuscated ? "ahk" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
			if (targetInsn == null)
				throw new RuntimeException("Couldn't find target instruction in PlayerControllerMP.onPlayerRightClick");
			transformIfCreativeBlock(method, targetInsn, getItemStackCheckInsns());

			method = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "clickBlock", "(IIII)V");
			if (method == null)
				throw new RuntimeException("Couldn't find PlayerControllerMP.clickBlock");
			targetInsn = findTargetInsn(method, isObfuscated ? "ahk" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
			if (targetInsn == null)
				throw new RuntimeException("Couldn't find target instruction in PlayerControllerMP.clickBlock");
			transformIfCreativeBlock(method, targetInsn, getClientCoordinatesCheckInsns(classNode));

			method = findMethodNodeOfClass(classNode, isObfuscated ? "c" : "onPlayerDamageBlock", "(IIII)V");
			if (method == null)
				throw new RuntimeException("Couldn't find PlayerControllerMP.onPlayerDamageBlock");
			targetInsn = findTargetInsn(method, isObfuscated ? "ahk" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
			if (targetInsn == null)
				throw new RuntimeException("Couldn't find target instruction in PlayerControllerMP.onPlayerDamageBlock");
			transformIfCreativeBlock(method, targetInsn, getClientCoordinatesCheckInsns(classNode));
			
			//TODO: onPlayerDestroyBlock
			// two method-specific isCreative checks

			return writeClassToBytes(classNode);
		}
		return bytes;
	}

	private ClassNode readClassFromBytes(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}

	private byte[] writeClassToBytes(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private MethodNode findMethodNodeOfClass(ClassNode classNode, String methodName, String methodDesc)
	{
		for (MethodNode method : classNode.methods)
		{
			if (method.name.equals(methodName) && method.desc.equals(methodDesc))
			{
				return method;
			}
		}
		return null;
	}

	private AbstractInsnNode findTargetInsn(MethodNode method, String owner, String name, String desc)
	{
		for (AbstractInsnNode curInsn = method.instructions.getFirst(); curInsn != null; curInsn = curInsn.getNext())
		{
			if (curInsn.getOpcode() == Opcodes.INVOKEVIRTUAL
					&& ((MethodInsnNode) curInsn).owner.equals(owner)
					&& ((MethodInsnNode) curInsn).name.equals(name)
					&& ((MethodInsnNode) curInsn).desc.equals(desc)
					&& curInsn.getNext() != null
					&& curInsn.getNext().getOpcode() == Opcodes.IFEQ
					&& curInsn.getNext().getNext() != null
					&& curInsn.getNext().getNext().getType() == AbstractInsnNode.LABEL)
			{
				return curInsn;
			}
		}
		return null;
	}

	private InsnList getItemStackCheckInsns()
	{
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CreativeBlocks.class), "isCreativeBlock", "(Lnet/minecraft/item/ItemStack;)Z", false));
		return insns;
	}

	private InsnList getClientCoordinatesCheckInsns(ClassNode classNode)
	{
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, isObfuscated ? "a" : "mc", isObfuscated ? "Lbao;" : "Lnet/minecraft/client/Minecraft;"));
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, isObfuscated ? "bao" : "net/minecraft/client/Minecraft", isObfuscated ? "f" : "theWorld", isObfuscated ? "Lbjf;" : "Lnet/minecraft/client/multiplayer/WorldClient;"));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CreativeBlocks.class), "isCreativeBlock", "(Lnet/minecraft/world/World;III)Z", false));
		return insns;
	}

	private InsnList getServerCoordinatesCheckInsns(ClassNode classNode)
	{
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, isObfuscated ? "a" : "theWorld", isObfuscated ? "Lahb;" : "Lnet/minecraft/world/World;"));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CreativeBlocks.class), "isCreativeBlock", "(Lnet/minecraft/world/World;III)Z", false));
		return insns;
	}

	private void transformIfCreativeBlock(MethodNode method, AbstractInsnNode targetInsn, InsnList checkInsns)
	{
		JumpInsnNode ifCreative = (JumpInsnNode) targetInsn.getNext();
		LabelNode afterIf = ifCreative.label;
		LabelNode insideIf = (LabelNode) ifCreative.getNext();

		ifCreative.label = insideIf;
		ifCreative.setOpcode(Opcodes.IFNE);

		InsnList toInject = new InsnList();
		toInject.add(checkInsns);
		toInject.add(new JumpInsnNode(Opcodes.IFEQ, afterIf));

		method.instructions.insert(ifCreative, toInject);
	}
}
