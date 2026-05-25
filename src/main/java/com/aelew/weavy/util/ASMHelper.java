package com.aelew.weavy.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

// yoinked from https://github.com/marioparaschiv/unrestricted/blob/main/src/main/java/me/youded/unrestricted/transformer/UnrestrictedTransformer.java
public final class ASMHelper {

    private ASMHelper() {
        throw new UnsupportedOperationException();
    }

    public static String findSingleBooleanField(final ClassNode classNode) {
        String found = null;

        for (final FieldNode f : classNode.fields) {
            if (!"Z".equals(f.desc)) {
                continue;
            }

            if ((f.access & Opcodes.ACC_STATIC) != 0) {
                continue;
            }

            if (found != null) {
                return null;
            }

            found = f.name;
        }

        return found;
    }

    public static MethodNode findFieldGetter(final ClassNode classNode, final String fieldName) {
        for (final MethodNode m : classNode.methods) {
            if (!"()Z".equals(m.desc)) {
                continue;
            }

            if ((m.access & Opcodes.ACC_STATIC) != 0) {
                continue;
            }

            if (loadsOnlyField(m, classNode.name, fieldName, "Z")) {
                return m;
            }
        }

        return null;
    }

    public static MethodNode findFieldSetter(final ClassNode classNode, final String fieldName) {
        for (final MethodNode m : classNode.methods) {
            if (!"(Z)V".equals(m.desc)) {
                continue;
            }

            if ((m.access & Opcodes.ACC_STATIC) != 0) {
                continue;
            }

            if (storesOnlyField(m, classNode.name, fieldName, "Z")) {
                return m;
            }
        }

        return null;
    }

    private static boolean loadsOnlyField(final MethodNode m, final String owner, final String name, final String desc) {
        boolean sawGetfield = false;

        for (final AbstractInsnNode insn : m.instructions) {
            final int op = insn.getOpcode();
            if (op < 0) {
                continue;
            }

            if (insn instanceof FieldInsnNode) {
                final FieldInsnNode fi = (FieldInsnNode) insn;

                if (op != Opcodes.GETFIELD) {
                    return false;
                }

                if (!fi.owner.equals(owner) || !fi.name.equals(name) || !fi.desc.equals(desc)) {
                    return false;
                }

                sawGetfield = true;
            }
        }

        return sawGetfield;
    }

    private static boolean storesOnlyField(final MethodNode m, final String owner, final String name, final String desc) {
        boolean sawPutfield = false;

        for (final AbstractInsnNode insn : m.instructions) {
            final int op = insn.getOpcode();
            if (op < 0) {
                continue;
            }

            if (insn instanceof FieldInsnNode) {
                final FieldInsnNode fi = (FieldInsnNode) insn;

                if (op != Opcodes.PUTFIELD) {
                    return false;
                }

                if (!fi.owner.equals(owner) || !fi.name.equals(name) || !fi.desc.equals(desc)) {
                    return false;
                }

                sawPutfield = true;
            }
        }

        return sawPutfield;
    }

}
