/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.program;

/**
 * @author Kiarash
 *
 */
public class Block {
	public enum BlockType {
		IF, ELSE, INIT
	};

	private BlockType type;

	public BlockType getType() {
		return type;
	}

	public int getDepth() {
		return depth;
	}

	public int getEnclosing_if_id() {
		return enclosing_if_id;
	}

	private int depth;
	private int enclosing_if_id;

	public Block(BlockType type, int depth, int enclosing_if_id) {
		this.type = type;
		this.depth = depth;
		this.enclosing_if_id = enclosing_if_id;
	}

	public boolean isEqual(Block other) {
		return this.depth == other.depth && this.type == other.type && this.enclosing_if_id == other.enclosing_if_id;
	}

	public String toString() {
		return "B(type: " + type + " if: " + this.enclosing_if_id + " depth: " + this.depth + ")";
	}

}
