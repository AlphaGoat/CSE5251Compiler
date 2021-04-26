! define SYS_exit = 1 (from sys/syscalls.h)
.set SYS_exit, 1

! define SP_TRAP_LINUX -- Linux System Call (from asm/traps.h)
.set SP_TRAP_LINUX, 0x90

! exit -- trap to operating system
.macro exit_program
set  stdout, %o0     ! first arg, %c0 := stdout
call fflush          ! call fflush from C library (libc); requires -lc
nop                  ! delay slot for pipelining
clr  %o0 			 ! %o0 := 0; program status=0=success
mov  SYS_exit, %g1   ! %g1 := SYS_exit; determine system call
ta   SP_TRAP_LINUX
.endm

.macro print_int
MOV %i0, %o0
call runtime_print_int
  nop
ret
restore
.endm