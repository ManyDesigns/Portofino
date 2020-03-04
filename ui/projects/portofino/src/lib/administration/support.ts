export class ConnectionProviderSummary {
  name: string;
  status: string;
  description: string;
}

export class ConnectionProviderDetails {
  databaseName: { value: string };
  driver: { value: string };
  errorMessage: { value: string };
  falseString: { value: string };
  hibernateDialect: { value: string };
  jndiResource: { value: string };
  lastTested: { value: number; displayValue: string };
  password: { value: string };
  schemas: { catalog: string; name: string; schema: string; selected: boolean }[];
  status: { value: string };
  trueString: { value: string };
  url: { value: string };
  user: { value: string };
  username: { value: string };
  entityMode: { value: string };
}

export class DatabasePlatform {
  connectionStringTemplate: string;
  description: string;
  standardDriverClassName: string;
  status: string;
}
