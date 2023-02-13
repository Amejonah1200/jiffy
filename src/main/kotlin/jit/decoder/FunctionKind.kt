package io.github.vbe0201.jiffy.jit.decoder

/**
 * Function types which may be selected by {@link InstructionKind#SPECIAL}.
 */
enum class FunctionKind(val opcode: UByte) {
    SLL(0x0U),
    SRL(0x2U),
    SRA(0x3U),
    SLLV(0x4U),
    SRLV(0x6U),
    SRAV(0x7U),
    JR(0x8U),
    JALR(0x9U),
    SYSCALL(0xCU),
    BREAK(0xDU),
    MFHI(0x10U),
    MTHI(0x11U),
    MFLO(0x12U),
    MTLO(0x13U),
    MULT(0x18U),
    MULTU(0x19U),
    DIV(0x1AU),
    DIVU(0x1BU),
    ADD(0x20U),
    ADDU(0x21U),
    SUB(0x22U),
    SUBU(0x23U),
    AND(0x24U),
    OR(0x25U),
    XOR(0x26U),
    NOR(0x27U),
    SLT(0x2AU),
    SLTU(0x2BU)
}
