package snownee.snow.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMTransformer implements IClassTransformer
{

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (!"net.minecraft.world.World".equals(transformedName))
        {
            return basicClass;
        }
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods)
        {
            if (methodNode.name.equals("canSnowAtBody"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                list.add(new VarInsnNode(Opcodes.ILOAD, 2));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "snownee/snow/Hook", "trySnowAt", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)V", false));
                methodNode.instructions.insert(list);
                break;
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
