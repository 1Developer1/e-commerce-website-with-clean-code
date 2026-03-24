/**
 * RFC 7807 Problem Detail parser.
 * Backend's GlobalExceptionHandler returns this format for all errors.
 */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
}

export function isProblemDetail(data: unknown): data is ProblemDetail {
  return (
    typeof data === 'object' &&
    data !== null &&
    'type' in data &&
    'title' in data &&
    'status' in data
  );
}

export function formatProblemDetail(problem: ProblemDetail): string {
  return problem.detail || problem.title || 'Beklenmeyen bir hata oluştu';
}
