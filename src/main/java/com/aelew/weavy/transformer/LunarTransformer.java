package com.aelew.weavy.transformer;

import com.aelew.weavy.util.ASMHelper;
import com.aelew.weavy.util.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;

// yoinked from https://github.com/marioparaschiv/unrestricted/blob/main/src/main/java/me/youded/unrestricted/transformer/UnrestrictedTransformer.java
public final class LunarTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(final ClassLoader loader, final String className,
                            final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {
        if (!className.startsWith("com/moonsworth/lunar/")) {
            return null;
        }

        try {
            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassNode node = new ClassNode();
            reader.accept(node, 0);

            boolean changed = false;
            changed |= transformModBlacklist(node);
            changed |= transformStaffCheck(node);

            if (!changed) {
                return null;
            }

            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            node.accept(writer);

            return writer.toByteArray();
        } catch (final Throwable t) {
            Logger.error("transform error on {}: {}", className, t);
            return null;
        }
    }

    private boolean transformModBlacklist(final ClassNode classNode) {
        boolean changed = false;

        for (final MethodNode method : classNode.methods) {
            if (!(method.access == Opcodes.ACC_PUBLIC && method.desc.equals("(Lcom/google/gson/JsonElement;)V"))) {
                continue;
            }

            final boolean blacklistEnableClass = Arrays.stream(method.instructions.toArray())
                    .filter(LdcInsnNode.class::isInstance)
                    .map(LdcInsnNode.class::cast)
                    .map(it -> it.cst)
                    .anyMatch("modSettings"::equals);

            if (blacklistEnableClass) {
                method.instructions.clear();
                method.localVariables.clear();
                method.exceptions.clear();
                method.tryCatchBlocks.clear();
                method.instructions.add(new InsnNode(Opcodes.RETURN));

                Logger.info("blacklist patched: {}.{}", classNode.name, method.name);
                changed = true;
            }
        }

        return changed;
    }

    private static boolean transformStaffCheck(final ClassNode classNode) {
        boolean foundReference = false;

        for (final MethodNode m : classNode.methods) {
            for (final AbstractInsnNode insn : m.instructions) {
                if (insn instanceof LdcInsnNode && "Xray".equals(((LdcInsnNode) insn).cst)) {
                    foundReference = true;
                    break;
                }
            }
        }

        if (!foundReference) {
            return false;
        }


        final String boolField = ASMHelper.findSingleBooleanField(classNode);
        if (boolField == null) {
            return false;
        }

        final MethodNode getter = ASMHelper.findFieldGetter(classNode, boolField);
        if (getter == null) {
            return false;
        }

        final MethodNode setter = ASMHelper.findFieldSetter(classNode, boolField);
        if (setter == null) {
            return false;
        }

        getter.instructions.clear();
        getter.localVariables.clear();
        getter.exceptions.clear();
        getter.tryCatchBlocks.clear();

        getter.instructions.add(new InsnNode(Opcodes.ICONST_1));
        getter.instructions.add(new InsnNode(Opcodes.IRETURN));

        setter.instructions.clear();
        setter.localVariables.clear();
        setter.exceptions.clear();
        setter.tryCatchBlocks.clear();

        setter.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        setter.instructions.add(new InsnNode(Opcodes.ICONST_1));
        setter.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, classNode.name, boolField, "Z"));
        setter.instructions.add(new InsnNode(Opcodes.RETURN));

        Logger.info("staff check patched: {} (getter={}, setter={}, field={})", classNode.name, getter.name, setter.name, boolField);
        return true;
    }

}
