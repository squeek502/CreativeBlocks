package squeek.creativeblocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.creativeblocks.config.Config;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.9")
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
        if (transformedName.equals("net.minecraft.server.management.PlayerInteractionManager"))
        {
            ClassNode classNode = readClassFromBytes(bytes);
            MethodNode method;
            AbstractInsnNode targetInsn;

            /** {@link net.minecraft.server.management.PlayerInteractionManager#processRightClickBlock(EntityPlayer, World, ItemStack, EnumHand, BlockPos, EnumFacing, float, float, float)}  */
            method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "processRightClickBlock", isObfuscated ? "(Lzj;Laht;Ladq;Lqm;Lcj;Lcq;FFF)Lqo;" : "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;");
            if (method == null)
                throw new RuntimeException("Couldn't find PlayerInteractionManager.processRightClickBlock");
            targetInsn = findIsCreativeInsn(method, classNode.name, isObfuscated ? "d" : "isCreative", "()Z");
            if (targetInsn == null)
                throw new RuntimeException("Couldn't find target instruction (this.isCreative()Z) in PlayerInteractionManager.processRightClickBlock");
            transformIfCreativeBlock(method, targetInsn, getItemStackCheckInsns());

            if (Config.oneHitBreak)
            {
                /** {@link net.minecraft.server.management.PlayerInteractionManager#onBlockClicked(BlockPos, EnumFacing)} */
                method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "onBlockClicked", isObfuscated ? "(Lcj;Lcq;)V" : "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V");
                if (method == null)
                    throw new RuntimeException("Couldn't find PlayerInteractionManager.onBlockClicked");
                targetInsn = findIsCreativeInsn(method, classNode.name, isObfuscated ? "d" : "isCreative", "()Z");
                if (targetInsn == null)
                    throw new RuntimeException("Couldn't find target instruction in PlayerInteractionManager.onBlockClicked");
                transformIfCreativeBlock(method, targetInsn, getServerCoordinatesCheckInsns(classNode));

                /** {@link net.minecraft.server.management.PlayerInteractionManager#tryHarvestBlock(BlockPos)} */
                method = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "tryHarvestBlock", isObfuscated ? "(Lcj;)Z" : "(Lnet/minecraft/util/math/BlockPos;)Z");
                if (method == null)
                    throw new RuntimeException("Couldn't find PlayerInteractionManager.tryHarvestBlock");
                targetInsn = findIsCreativeInsn(method, classNode.name, isObfuscated ? "d" : "isCreative", "()Z");
                if (targetInsn == null)
                    throw new RuntimeException("Couldn't find target instruction in PlayerInteractionManager.tryHarvestBlock");
                transformIfCreativeBlock(method, targetInsn, getServerCoordinatesCheckInsns(classNode));
            }

            return writeClassToBytes(classNode);
        }
        else if (transformedName.equals("net.minecraft.client.multiplayer.PlayerControllerMP"))
        {
            ClassNode classNode = readClassFromBytes(bytes);
            MethodNode method;
            AbstractInsnNode targetInsn;

            /** {@link net.minecraft.client.multiplayer.PlayerControllerMP#processRightClickBlock(EntityPlayerSP, WorldClient, ItemStack, BlockPos, EnumFacing, Vec3d, EnumHand)} */
            method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "processRightClickBlock", isObfuscated ? "(Lbmt;Lbku;Ladq;Lcj;Lcq;Lbbj;Lqm;)Lqo;" : "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;");
            if (method == null)
                throw new RuntimeException("Couldn't find PlayerControllerMP.processRightClickBlock");
            targetInsn = findIsCreativeInsn(method, isObfuscated ? "ahw$a" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
            if (targetInsn == null)
                throw new RuntimeException("Couldn't find target instruction in PlayerControllerMP.onPlayerRightClick");
            transformIfCreativeBlock(method, targetInsn, getItemStackCheckInsns());

            if (Config.oneHitBreak)
            {
                /** {@link net.minecraft.client.multiplayer.PlayerControllerMP#clickBlock(BlockPos, EnumFacing)}*/
                method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "clickBlock", isObfuscated ? "(Lcj;Lcq;)Z" : "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z");
                if (method == null)
                    throw new RuntimeException("Couldn't find PlayerControllerMP.clickBlock");
                targetInsn = findIsCreativeInsn(method, isObfuscated ? "ahw$a" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
                if (targetInsn == null)
                    throw new RuntimeException("Couldn't find target instruction in PlayerControllerMP.clickBlock");
                transformIfCreativeBlock(method, targetInsn, getClientCoordinatesCheckInsns(classNode));

                /** {@link net.minecraft.client.multiplayer.PlayerControllerMP#onPlayerDamageBlock(BlockPos, EnumFacing)} */
                method = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "onPlayerDamageBlock", isObfuscated ? "(Lcj;Lcq;)Z" : "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z");
                if (method == null)
                    throw new RuntimeException("Couldn't find PlayerControllerMP.onPlayerDamageBlock");
                targetInsn = findIsCreativeInsn(method, isObfuscated ? "ahw$a" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
                if (targetInsn == null)
                    throw new RuntimeException("Couldn't find target instruction in PlayerControllerMP.onPlayerDamageBlock");
                transformOnPlayerDamageBlock(method, targetInsn, getClientCoordinatesCheckInsns(classNode));
            }

            // onPlayerDestroyBlock = func_187103_a
            /** {@link net.minecraft.client.multiplayer.PlayerControllerMP#func_187103_a(BlockPos)}  */
            method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "func_187103_a", isObfuscated ? "(Lcj;)Z" : "(Lnet/minecraft/util/math/BlockPos;)Z");
            if (method == null)
                throw new RuntimeException("Couldn't find PlayerControllerMP.onPlayerDestroyBlock/func_187103_a");
            transformOnPlayerDestroyBlock(classNode, method);

            return writeClassToBytes(classNode);
        }
        else if (transformedName.equals("net.minecraft.client.Minecraft"))
        {
            ClassNode classNode = readClassFromBytes(bytes);
            MethodNode method;

            /** {@link Minecraft#middleClickMouse()}  */
            method = findMethodNodeOfClass(classNode, isObfuscated ? "aC" : "middleClickMouse", "()V");
            if (method == null)
                throw new RuntimeException("Couldn't find Minecraft.middleClickMouse");

            AbstractInsnNode targetInsn = findFirstInstruction(method);

            InsnList toInject = new InsnList();
            toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CreativeBlocks.class), "onPickBlock", "()Z", false));
            LabelNode labelIfFalse = new LabelNode();
            toInject.add(new JumpInsnNode(Opcodes.IFEQ, labelIfFalse));
            toInject.add(new InsnNode(Opcodes.RETURN));
            toInject.add(labelIfFalse);

            method.instructions.insertBefore(targetInsn, toInject);

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
        try
        {
            classNode.accept(writer);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            CreativeBlocks.log.error("[CreativeBlocks] StringIndexOutOfBounds when writing transforming class. Class name: {} ", classNode.name, e.getCause());
        }
        catch (Exception e)
        {
            CreativeBlocks.log.error("[CreativeBlocks] Something went wrong when transforming Class {}", classNode.name, e.getCause());
        }
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

    private LabelNode findEndLabel(MethodNode method)
    {
        for (AbstractInsnNode instruction = method.instructions.getLast(); instruction != null; instruction = instruction.getPrevious())
        {
            if (instruction instanceof LabelNode)
                return (LabelNode) instruction;
        }
        return null;
    }

    private AbstractInsnNode findFirstInstruction(MethodNode method)
    {
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext())
        {
            if (instruction.getType() != AbstractInsnNode.LABEL && instruction.getType() != AbstractInsnNode.LINE)
                return instruction;
        }
        return null;
    }

    private AbstractInsnNode findIsCreativeInsn(MethodNode method, String owner, String name, String desc)
    {
        return findIsCreativeInsn(method.instructions.getFirst(), owner, name, desc);
    }

    private AbstractInsnNode findIsCreativeInsn(AbstractInsnNode firstInsnToCheck, String owner, String name, String desc)
    {
        for (AbstractInsnNode curInsn = firstInsnToCheck; curInsn != null; curInsn = curInsn.getNext())
        {
            if (curInsn.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && ((MethodInsnNode) curInsn).owner.equals(owner)
                    && ((MethodInsnNode) curInsn).name.equals(name)
                    && ((MethodInsnNode) curInsn).desc.equals(desc)
                    && curInsn.getNext() != null
                    && curInsn.getNext().getType() == AbstractInsnNode.JUMP_INSN)
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
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CreativeBlocks.class), "isCreativeBlock", isObfuscated ? "(Ladq;)Z" : "(Lnet/minecraft/item/ItemStack;)Z", false));
        return insns;
    }

    private InsnList getClientCoordinatesCheckInsns(ClassNode classNode)
    {
        InsnList insns = new InsnList();
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, isObfuscated ? "a" : "mc", isObfuscated ? "Lbcf;" : "Lnet/minecraft/client/Minecraft;"));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, isObfuscated ? "bcf" : "net/minecraft/client/Minecraft", isObfuscated ? "f" : "theWorld", isObfuscated ? "Lbku;" : "Lnet/minecraft/client/multiplayer/WorldClient;"));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CreativeBlocks.class), "isCreativeBlock", isObfuscated ? "(Laht;Lcj;)Z" : "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false));
        return insns;
    }

    private InsnList getServerCoordinatesCheckInsns(ClassNode classNode)
    {
        InsnList insns = new InsnList();
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, isObfuscated ? "a" : "theWorld", isObfuscated ? "Laht;" : "Lnet/minecraft/world/World;"));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CreativeBlocks.class), "isCreativeBlock", isObfuscated ? "(Laht;Lcj;)Z" : "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false));
        return insns;
    }

    private LabelNode getNextLabelNode(AbstractInsnNode node)
    {
        for (AbstractInsnNode currNode = node; currNode != null; currNode = currNode.getNext())
        {
            if (currNode instanceof LabelNode)
            {
                return (LabelNode) currNode;
            }
        }
        return null;
    }

    /**
     * {@link net.minecraft.client.multiplayer.PlayerControllerMP#onPlayerDamageBlock(BlockPos, EnumFacing)}
     * Method we're transforming changed in the 1.7-1.9 transition so had to make a specific method for this
     * method.
     */
    private void transformOnPlayerDamageBlock(MethodNode method, AbstractInsnNode targetInsn, InsnList checkInsn)
    {
        JumpInsnNode ifCreative = (JumpInsnNode) targetInsn.getNext();
        LabelNode afterIf = ifCreative.label;
        LabelNode insideIf = this.getNextLabelNode(targetInsn);

        ifCreative.label = insideIf;
        ifCreative.setOpcode(Opcodes.IFNE);

        InsnList toInject = new InsnList();
        toInject.add(checkInsn);
        toInject.add(new JumpInsnNode(Opcodes.IFEQ, afterIf));

        method.instructions.insert(ifCreative, toInject);
    }

    private void transformIfCreativeBlock(MethodNode method, AbstractInsnNode targetInsn, InsnList checkInsns)
    {
        JumpInsnNode ifCreative = (JumpInsnNode) targetInsn.getNext();
        LabelNode afterIf = ifCreative.label; // l49
        LabelNode insideIf = (LabelNode) ifCreative.getNext(); // l50

        ifCreative.label = insideIf;
        ifCreative.setOpcode(Opcodes.IFNE);

        InsnList toInject = new InsnList();
        toInject.add(checkInsns);
        toInject.add(new JumpInsnNode(Opcodes.IFEQ, afterIf));

        method.instructions.insert(ifCreative, toInject);
    }

    // two method-specific isCreative checks
    private void transformOnPlayerDestroyBlock(ClassNode classNode, MethodNode method)
    {
        // have to store the result because the second isCreative check
        // is after the block has been cleared
        LabelNode isCreativeBlockStart = new LabelNode();
        LabelNode end = findEndLabel(method);
        LocalVariableNode isCreativeBlock = new LocalVariableNode("isCreativeBlock", "Z", null, isCreativeBlockStart, end, method.maxLocals);
        method.maxLocals += 1;
        method.localVariables.add(isCreativeBlock);

        InsnList toInject = new InsnList();
        toInject.add(getClientCoordinatesCheckInsns(classNode));
        toInject.add(new VarInsnNode(Opcodes.ISTORE, isCreativeBlock.index));
        toInject.add(isCreativeBlockStart);

        method.instructions.insertBefore(findFirstInstruction(method), toInject);

        AbstractInsnNode targetInsn = findIsCreativeInsn(method, isObfuscated ? "ahw$a" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
        if (targetInsn == null)
            throw new RuntimeException("Couldn't find first target instruction in PlayerControllerMP.onPlayerDestroyBlock");

        JumpInsnNode ifCreative = (JumpInsnNode) targetInsn.getNext();
        LabelNode afterIf = ifCreative.label;
        LabelNode afterOr = new LabelNode();

        ifCreative.label = afterOr;
        ifCreative.setOpcode(Opcodes.IFNE);

        toInject = new InsnList();
        toInject.add(new VarInsnNode(Opcodes.ILOAD, isCreativeBlock.index));
        toInject.add(new JumpInsnNode(Opcodes.IFEQ, afterIf));
        toInject.add(afterOr);

        method.instructions.insert(ifCreative, toInject);

        targetInsn = findIsCreativeInsn(ifCreative, isObfuscated ? "ahw$a" : "net/minecraft/world/WorldSettings$GameType", isObfuscated ? "d" : "isCreative", "()Z");
        if (targetInsn == null)
            throw new RuntimeException("Couldn't find second target instruction in PlayerControllerMP.onPlayerDestroyBlock");

        ifCreative = (JumpInsnNode) targetInsn.getNext();
        afterIf = ifCreative.label;

        toInject = new InsnList();
        toInject.add(new VarInsnNode(Opcodes.ILOAD, isCreativeBlock.index));
        toInject.add(new JumpInsnNode(Opcodes.IFNE, afterIf));

        method.instructions.insert(ifCreative, toInject);
    }
}