export default function ErrorMessage({ error }: { error: unknown }) {
  const message = error instanceof Error ? error.message : String(error);
  return (
    <div className="alert alert-danger" role="alert">
      {message}
    </div>
  );
}
