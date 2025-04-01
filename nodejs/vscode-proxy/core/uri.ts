export class Uri {
  private filePath: string;

  private constructor(filePath: string) {
    this.filePath = filePath;
  }

  static file(path: string): Uri {
    return new Uri(path);
  }

  static parse(path: string): Uri {
    return new Uri(path);
  }

  get fsPath(): string {
    return this.filePath;
  }

  with(options: { query?: string | undefined; fragment?: string | undefined; }): Uri {
    var path = this.filePath
    if (options.query) {
      path += '?' + options.query
    }
    if (options.fragment) {
      path += '#' + options.fragment
    }
    return new Uri(path)
  }

  static joinPath(uri: Uri,...pathSegments: string[]): Uri {
    return new Uri(uri.fsPath + '/' + pathSegments.join('/'))
  }


  toString(): string {
    return this.filePath
  }
}
