package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.codegen.Condition
import io.github.vbe0201.jiffy.jit.codegen.Conditional
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.decoder.FunctionKind
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.utils.*

// Reserved local variable slot for checked results of computations.
private const val CHECKED_RESULT_SLOT = 3

private fun computeSignedOverflow(
    insn: Instruction,
    emitter: BytecodeEmitter,
    action: Conditional.() -> Unit
) {
    // Check if an overflow has occurred. This is the case when both
    // operands have a different sign than the result.
    emitter.run {
        loadLocal(CHECKED_RESULT_SLOT)
        getGpr(insn.rs())
        ixor(null)

        loadLocal(CHECKED_RESULT_SLOT)
        if (insn.function() == FunctionKind.ADD) {
            getGpr(insn.rt())
            ixor(null)
        } else {
            ixor(insn.imm().signExtend32())
        }

        iand(null)
        conditional(Condition.SMALLER_THAN_ZERO, action)
    }
}

/**
 * Generates the Add Immediate Unsigned (ADDIU) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun addiu(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        // NOTE: The "Unsigned" part in the instruction name is
        // misleading. The sign extension is intentional.
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the ADD instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun add(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    // Compute the sum of both operands and store as local variable.
    emitter.run {
        getGpr(insn.rs())
        getGpr(insn.rt())
        iadd(null)
        storeLocal(CHECKED_RESULT_SLOT)
    }

    // Check for signed overflow.
    computeSignedOverflow(insn, emitter) {
        then = {
            // TODO: Raise an exception for overflow.
            generateUnimplementedStub()
        }

        orElse = {
            // When we were successful, write the sum to output.
            setGpr(insn.rd()) {
                loadLocal(CHECKED_RESULT_SLOT)
            }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Add Immediate (ADDI) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun addi(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    // Compute the sum of both operands and store as local variable.
    emitter.run {
        getGpr(insn.rs())
        iadd(insn.imm().signExtend32())
        storeLocal(CHECKED_RESULT_SLOT)
    }

    // Check for signed overflow.
    computeSignedOverflow(insn, emitter) {
        then = {
            // TODO: Raise an exception for overflow.
            generateUnimplementedStub()
        }

        orElse = {
            // When we were successful, write the sum to output.
            setGpr(insn.rt()) {
                loadLocal(CHECKED_RESULT_SLOT)
            }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Add Unsigned (ADDU) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun addu(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())
        iadd(null)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Load Unsigned Immediate (LUI) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun lui(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        push(insn.imm().zeroExtend32() shl 16)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the AND Immediate (ANDI) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun andi(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        getGpr(insn.rs())
        iand(insn.imm().zeroExtend32())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the OR Immediate (ORI) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun ori(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        getGpr(insn.rs())
        ior(insn.imm().zeroExtend32())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the XOR Immediate (XORI) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun xori(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        getGpr(insn.rs())
        ixor(insn.imm().zeroExtend32())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the AND instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun and(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())
        iand(null)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the OR instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun or(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())
        ior(null)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the XOR instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun xor(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())
        ixor(null)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the bitwise NOT OR (NOR) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun nor(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        // Bitwise OR both registers together.
        getGpr(insn.rs())
        getGpr(insn.rt())
        ior(null)

        // Compute the complement of the resulting value.
        ixor((-1).toUInt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Left Logical (SLL) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sll(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rt())
        ishl(insn.shamt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Left Logical Variable (SLLV) instruction
 * to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sllv(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rt())
        getGpr(insn.rs())

        // NOTE: The ISHL instruction truncates the S register
        // to 5 bits already, as defined by the MIPS architecture.
        ishl(null)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Logical (SRL) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun srl(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rt())
        iushr(insn.shamt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Logical Variable (SRLV) instruction
 * to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun srlv(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rt())
        getGpr(insn.rs())

        // NOTE: The IUSHR instruction truncates the S register
        // to 5 bits already, as defined by the MIPS architecture.
        iushr(null)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Arithmetic (SRA) instruction to the
 * code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sra(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rt())
        ishr(insn.shamt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Shift Right Arithmetic Variable (SRLV) instruction
 * to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun srav(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rt())
        getGpr(insn.rs())

        // NOTE: The ISHR instruction truncates the S register
        // to 5 bits already, as defined by the MIPS architecture.
        ishr(null)
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set if Less Than Immediate (SLTI) instruction
 * to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun slti(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        getGpr(insn.rs())
        push(insn.imm().signExtend32())

        conditional(Condition.INT_SMALLER_THAN) {
            then = { push(1U) }
            orElse = { push(0U) }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set if Less Than Immediate Unsigned (SLTIU)
 * instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sltiu(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rt()) {
        getGpr(insn.rs())
        push(insn.imm().signExtend32())

        conditional(Condition.UNSIGNED_INT_SMALLER_THAN) {
            then = { push(1U) }
            orElse = { push(0U) }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set on Less Than (SLT) instruction to the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun slt(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())

        conditional(Condition.INT_SMALLER_THAN) {
            then = { push(1U) }
            orElse = { push(0U) }
        }
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Set on Less Than Unsigned (SLTU) instruction to
 * the code buffer.
 */
@Suppress("UNUSED_PARAMETER")
fun sltu(pc: UInt, insn: Instruction, emitter: BytecodeEmitter): Status {
    emitter.setGpr(insn.rd()) {
        getGpr(insn.rs())
        getGpr(insn.rt())

        conditional(Condition.UNSIGNED_INT_SMALLER_THAN) {
            then = { push(1U) }
            orElse = { push(0U) }
        }
    }

    return Status.CONTINUE_BLOCK
}
