/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * A position in the editor. This interface is suitable for serialization.
 */
export interface IPosition {
	/**
	 * line number (starts at 1)
	 */
	readonly line: number;
	/**
	 * column (the first character in a line is between column 1 and column 2)
	 */
	readonly column: number;
}

/**
 * A position in the editor.
 */
export class Position {
	/**
	 * line number (starts at 1)
	 */
	public readonly line: number;
	/**
	 * column (the first character in a line is between column 1 and column 2)
	 */
	public readonly column: number;

	constructor(line: number, column: number) {
		this.line = line;
		this.column = column;
	}

	/**
	 * Create a new position from this position.
	 *
	 * @param newline new line number
	 * @param newColumn new column
	 */
	with(newline: number = this.line, newColumn: number = this.column): Position {
		if (newline === this.line && newColumn === this.column) {
			return this;
		} else {
			return new Position(newline, newColumn);
		}
	}

	/**
	 * Derive a new position from this position.
	 *
	 * @param deltaline line number delta
	 * @param deltaColumn column delta
	 */
	delta(deltaline: number = 0, deltaColumn: number = 0): Position {
		return this.with(this.line + deltaline, this.column + deltaColumn);
	}

 	translate(deltaline: number = 0, deltaColumn: number = 0): Position {
 		return this.with(this.line + deltaline, this.column + deltaColumn);
	}


	/**
	 * Test if this position equals other position
	 */
	public equals(other: IPosition): boolean {
		return Position.equals(this, other);
	}

	/**
	 * Test if position `a` equals position `b`
	 */
	public static equals(a: IPosition | null, b: IPosition | null): boolean {
		if (!a && !b) {
			return true;
		}
		return (
			!!a &&
			!!b &&
			a.line === b.line &&
			a.column === b.column
		);
	}

	/**
	 * Test if this position is before other position.
	 * If the two positions are equal, the result will be false.
	 */
	public isBefore(other: IPosition): boolean {
		return Position.isBefore(this, other);
	}

	/**
	 * Test if position `a` is before position `b`.
	 * If the two positions are equal, the result will be false.
	 */
	public static isBefore(a: IPosition, b: IPosition): boolean {
		if (a.line < b.line) {
			return true;
		}
		if (b.line < a.line) {
			return false;
		}
		return a.column < b.column;
	}

	/**
	 * Test if this position is before other position.
	 * If the two positions are equal, the result will be true.
	 */
	public isBeforeOrEqual(other: IPosition): boolean {
		return Position.isBeforeOrEqual(this, other);
	}

	/**
	 * Test if position `a` is before position `b`.
	 * If the two positions are equal, the result will be true.
	 */
	public static isBeforeOrEqual(a: IPosition, b: IPosition): boolean {
		if (a.line < b.line) {
			return true;
		}
		if (b.line < a.line) {
			return false;
		}
		return a.column <= b.column;
	}

	/**
	 * A function that compares positions, useful for sorting
	 */
	public static compare(a: IPosition, b: IPosition): number {
		const aline = a.line | 0;
		const bline = b.line | 0;

		if (aline === bline) {
			const aColumn = a.column | 0;
			const bColumn = b.column | 0;
			return aColumn - bColumn;
		}

		return aline - bline;
	}

	/**
	 * Clone this position.
	 */
	public clone(): Position {
		return new Position(this.line, this.column);
	}

	/**
	 * Convert to a human-readable representation.
	 */
	public toString(): string {
		return '(' + this.line + ',' + this.column + ')';
	}

	// ---

	/**
	 * Create a `Position` from an `IPosition`.
	 */
	public static lift(pos: IPosition): Position {
		return new Position(pos.line, pos.column);
	}

	/**
	 * Test if `obj` is an `IPosition`.
	 */
	public static isIPosition(obj: any): obj is IPosition {
		return (
			obj
			&& (typeof obj.line === 'number')
			&& (typeof obj.column === 'number')
		);
	}

	public toJSON(): IPosition {
		return {
			line: this.line,
			column: this.column
		};
	}
}
