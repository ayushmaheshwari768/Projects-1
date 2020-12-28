.include "./cs47_proj_macro.asm"
.include "./cs47_common_macro.asm"
.text
.globl au_logical
# TBD: Complete your project procedures
# Needed skeleton is given
#####################################################################
# Implement au_logical
# Argument:
# 	$a0: First number
#	$a1: Second number
#	$a2: operation code ('+':add, '-':sub, '*':mul, '/':div)
# Return:
#	$v0: ($a0+$a1) | ($a0-$a1) | ($a0*$a1):LO | ($a0 / $a1)
# 	$v1: ($a0 * $a1):HI | ($a0 % $a1)
# Notes:
#####################################################################
au_logical:
	addi	$sp, $sp, -52
	sw	$a0, 52($sp)
	sw	$a1, 48($sp)
	sw	$a2, 44($sp)
	sw	$s0, 40($sp)
	sw 	$s1, 36($sp)
	sw	$s2, 32($sp)
	sw	$s3, 28($sp)
	sw	$s5, 24($sp)
	sw	$s6, 20($sp)
	sw	$s7, 16($sp)
	sw 	$fp, 12($sp)
	sw 	$ra, 8($sp)
	addi 	$fp, $sp, 52
	
	beq	$a2, '+', add_logical
	beq	$a2, '-', sub_logical
	beq	$a2, '*', mult_logical
	beq	$a2, '/', div_logical
add_logical:
	move	$s2, $zero	#s2 = carry in 
	move	$v0, $zero
	j	adder_subtractor
sub_logical:
	addi	$s2, $zero, 1	#s2 = carry in
	not	$a1, $a1
	move	$v0, $zero
	j	adder_subtractor
mult_logical:
	move	$v0, $a1	
	not	$v0, $v0	#to calculate two's complement
	li	$t4, 1		#32-bit 1's to use throughout multiplication code
	move	$t3, $zero	#t3 = addition counter
	move	$t7, $zero	#reset carry in just in case it is 1
	move	$v1, $zero
	move	$t9, $a0 	#t9 = original value of a0 before two's complement
	not	$a0, $a0	#v1 = upper 32 bits of answer
	move	$t2, $zero	#t2 = multiplication counter
	move	$s0, $zero	#s0 = will hold two's complement of mplr
	move	$s1, $zero	#s1 = will hold two's complement of mcnd
mplr_adder:
	beq	$t3, 32, mcnd_adder
	nth_bit($t3, $v0, $t5)	#t5 = A
	nth_bit($t3, $t4, $t6)	#t6 = B
	full_adder($t5, $t6, $t7, $t8)	#t7 = CI, t8 = Y
	sllv	$t8, $t8, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s0, $s0, $t8	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	mplr_adder
mcnd_adder:
	move	$t3, $zero	#counter reset to zero
	move	$t7, $zero	#carry in reset to zero
mcnd_adder_loop:
	beq	$t3, 32, after_two_complement
	nth_bit($t3, $a0, $t5)	#t5 = A
	nth_bit($t3, $t4, $t6)	#t6 = B
	full_adder($t5, $t6, $t7, $t8)	#t7 = CI, t8 = Y
	sllv	$t8, $t8, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s1, $s1, $t8	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	mcnd_adder_loop
after_two_complement:
	move	$t3, $zero	#reset counter
	li	$t4, 31
	move	$v0, $s0	#move temporary sums back into correct registers
	move	$a0, $s1
	move	$s0, $zero	#reset register to use for other purpose
	move	$s1, $zero	#reset register to use for other purpose
	nth_bit($t4, $t9, $t5)
	beqz	$t5, mcnd_first_bit	#t5 will hold the first bit of mcnd
after_checking_mcnd_first_bit:
	nth_bit($t4, $a1, $t6)	
	beqz	$t6, mplr_first_bit
mult_loop:
	beq	$t2, 32, after_mult_loop
	nth_bit($zero, $v0, $s0)		#s0 = MPLR[0]
	bnez	$s0, not_zero
after_checking_nth_bit:
	and	$s1, $s0, $a0	#what to add to product
	move	$t3, $zero	#t3 = addition counter
	move	$s6, $zero	#s6 = temporary sum
mult_adder:
	beq	$t3, 32, after_new_product_value
	nth_bit($t3, $s1, $s2)	#s2 = A
	nth_bit($t3, $v1, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s6, $s6, $s5	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	mult_adder
after_new_product_value:
	move	$v1, $s6
	connected_registers_shift($v1, $v0)
	addi	$t2, $t2, 1
	j	mult_loop
after_mult_loop:
	xor	$t7, $t5, $t6	#t7 is the condition of the last multiplexer
	move	$t2, $zero	#reset multiplication counter to use for higher bits
	move	$t3, $zero	#reset addition counter to use for lower bits
	not	$t8, $v0	#t8 = not of lower bits of product
	not	$t9, $v1	#t9 = not of higher bits of product
	move	$t5, $zero	#reset temporary sums
	move	$t6, $zero
	li	$t4, 1		#t4 = to use in two's complement conversions
last_adder_lower_bits:
	beq	$t3, 32, last_adder_upper_bits
	nth_bit($t3, $t8, $s2)	#s2 = A
	nth_bit($t3, $t4, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$t5, $t5, $s5	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	last_adder_lower_bits
last_adder_upper_bits:
	beq	$t2, 32, after_last_adder
	nth_bit($t2, $t9, $s2)	#s2 = A
	nth_bit($t2, $zero, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t2	#Y is shifted however many bits to the left it should (based on the counter)
	or	$t6, $t6, $s5	#or of current product and 
	addi	$t2, $t2, 1	#increment counter
	j	last_adder_lower_bits
after_last_adder:
	beqz	$t7, Restore
	move	$v0, $t5
	move	$v1, $t6
	j	Restore 
not_zero:
	li	$s0, 0xFFFFFFFF  
	j	after_checking_nth_bit
mcnd_first_bit:
	move	$a0, $t9	#t9 was before two's complement value
	j 	after_checking_mcnd_first_bit
mplr_first_bit:
	move	$v0, $a1	#a1 was before two's complement value
	j	mult_loop
div_logical:
	move	$t2, $zero	#t2 = division counter
	move 	$t3, $zero	#t3 = addition/subtraction counter
	li	$s0, 1		
	move	$v0, $a0
	not	$a0, $a0
	move	$v1, $zero
	not	$t4, $a1
	li	$t5, 31
	move	$s6, $zero	#temporary sum
dvnd_two_complement:
	beq	$t3, 32, dividend_multiplexer
	nth_bit($t3, $s0, $s2)	#s2 = A
	nth_bit($t3, $a0, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s6, $s6, $s5	#or of current product and temp sum
	addi	$t3, $t3, 1	#increment counter
	j	dvnd_two_complement
dividend_multiplexer:
	move	$s7, $zero
	move	$t3, $zero
	nth_bit($t5, $v0, $t8)
	bnez	$t8, negative_dividend
	move	$s6, $zero
	j	dvsr_two_complement
negative_dividend:
	move	$v0, $s6
	move	$s6, $zero
dvsr_two_complement:
	beq	$t3, 32, divisor_multiplexer
	nth_bit($t3, $t4, $s2)	#s2 = A
	nth_bit($t3, $s0, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s6, $s6, $s5	#or of current product and temp sum
	addi	$t3, $t3, 1	#increment counter
	j	dvsr_two_complement
divisor_multiplexer:	
	move	$s7, $zero
	move	$t3, $zero
	nth_bit($t5, $a1, $t9)
	bnez	$t9, negative_divisor
	move	$s6, $zero
	j	div_loop
negative_divisor:
	move	$t4, $s6
	move	$a1, $s6	#change argument for roll back purposes (found in debugging)
	not 	$t4, $t4
	move 	$s6, $zero
div_loop:
	beq	$t2, 32, after_divider
	move	$s7, $s0	#s7 = carry in and is set to 1 to subtract
	move	$t3, $zero	#t3 = add/subtract counter
	move	$s6, $zero	#s6 = temporary sum
	connected_registers_shift_left($v1, $v0)
div_subtract:
	beq	$t3, 32, after_new_remainder_value
	nth_bit($t3, $t4, $s2)	#s2 = A
	nth_bit($t3, $v1, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s6, $s6, $s5	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	div_subtract
after_new_remainder_value:
	move	$v1, $s6
	move 	$t3, $zero
	move	$s6, $zero
	move	$s7, $zero
	nth_bit($t5, $v1, $t6)
	bnez	$t6, roll_back
	j	after_potential_roll_back
after_roll_back:
	move	$v1, $s6
after_potential_roll_back:
	beqz	$t6, shift_one_into_quotient
after_shifting_one:
	addi	$t2, $t2, 1
	j 	div_loop
shift_one_into_quotient:
	or	$v0, $v0, $s0
	j	after_shifting_one
roll_back:
	beq	$t3, 32, after_roll_back
	nth_bit($t3, $a1, $s2)	#s2 = A
	nth_bit($t3, $v1, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s6, $s6, $s5	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	roll_back
after_divider:
	xor	$t6, $t8, $t9
	move	$t3, $zero	#counter reset to zero
	move	$s6, $zero	#reset temporary sum again
	move	$s7, $zero	#reset carry in
	not	$t4, $v0
quotient_two_complement:
	beq	$t3, 32, quotient_multiplexer
	nth_bit($t3, $s0, $s2)	#s2 = A
	nth_bit($t3, $t4, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s6, $s6, $s5	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	quotient_two_complement
quotient_multiplexer:
	move	$t3, $zero	#reset counter
	move	$s7, $zero	#reset carry in
	not	$t4, $v1
	bnez	$t6, negative_quotient
	move	$s6, $zero
	j	remainder_two_complement
negative_quotient:
	move	$v0, $s6
	move	$s6, $zero
	j	remainder_two_complement
remainder_two_complement:
	beq	$t3, 32, remainder_multiplexer
	nth_bit($t3, $s0, $s2)	#s2 = A
	nth_bit($t3, $t4, $s3)	#s3 = B
	full_adder($s2, $s3, $s7, $s5)	#s5 = Y
	sllv	$s5, $s5, $t3	#Y is shifted however many bits to the left it should (based on the counter)
	or	$s6, $s6, $s5	#or of current product and 
	addi	$t3, $t3, 1	#increment counter
	j	remainder_two_complement
remainder_multiplexer:
	move	$t3, $zero	#reset counter
	move	$s7, $zero	#reset carry in
	move	$t4, $zero
	bnez	$t8, negative_remainder
	move	$s6, $zero
	j	Restore
negative_remainder:
	move	$v1, $s6
	move	$s6, $zero
	j	Restore
adder_subtractor:
	move	$t2, $zero	#t2 = counter
adder_subtractor_loop:
	beq	$t2, 32, Restore
	nth_bit($t2, $a0, $s0)	#s0 = A
	nth_bit($t2, $a1, $s1)	#s1 = B
	full_adder($s0, $s1, $s2, $s3)	#s3 = Y
	sllv	$s3, $s3, $t2	#Y is shifted however many bits to the left it should (based on the counter)
	or	$v0, $v0, $s3	#or of current product and 
	addi	$t2, $t2, 1	#increment counter
	j	adder_subtractor_loop
Restore:	
	lw	$a0, 52($sp)
	lw	$a1, 48($sp)
	lw	$a2, 44($sp)
	lw	$s0, 40($sp)
	lw 	$s1, 36($sp)
	lw	$s2, 32($sp)
	lw	$s3, 28($sp)
	lw	$s5, 24($sp)
	lw	$s6, 20($sp)
	lw	$s7, 16($sp)
	lw 	$fp, 12($sp)
	lw 	$ra, 8($sp)
	addi 	$sp, $sp, 52
	
	jr 	$ra
