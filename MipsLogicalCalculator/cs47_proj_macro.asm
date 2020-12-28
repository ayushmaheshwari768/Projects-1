# Add you macro definition here - do not touch cs47_common_macro.asm"
#<------------------ MACRO DEFINITIONS ---------------------->#
.macro nth_bit($regNumberBit, $regNumber, $regNthBit)
addi	$regNthBit, $zero, 1
sllv	$regNthBit, $regNthBit, $regNumberBit
and	$regNthBit, $regNumber, $regNthBit
srlv	$regNthBit, $regNthBit, $regNumberBit
.end_macro 

.macro full_adder($regA, $regB, $regCI, $regY)
xor	$t0, $regA, $regB	#t0 = A xor B
xor	$regY, $t0, $regCI	#Y is calculated here
and 	$t1, $t0, $regCI	#t1 = CI.(A xor B)
and 	$t0, $regA, $regB	#t0 = A.B
or 	$regCI, $t0, $t1	#carry in can be set to carry out value for next iteration
.end_macro

.macro connected_registers_shift($reg1, $reg2)	#simulates the connected product and multiplier register
addi	$t0, $zero, 1		#next two lines get nth_bit (copy of nth_bit macro)
and	$t0, $reg1, $t0
sll	$t0, $t0, 31
srl	$reg1, $reg1, 1
srl	$reg2, $reg2, 1
or	$reg2, $reg2, $t0	#32nd bit of multiplicand is 0 so or with shifted out bit of product will correct it to 1 or 0
.end_macro

.macro connected_registers_shift_left($reg1, $reg2)
li	$t0, 1			#next four lines follow logic of nth_bit macro to get nth bit
sll	$t0, $t0, 31
and 	$t0, $reg2, $t0
srl	$t0, $t0, 31
sll	$reg1, $reg1, 1
sll	$reg2, $reg2, 1
or	$reg1, $reg1, $t0
.end_macro
		
