import { HttpService } from '@nestjs/axios';
import { Injectable, Logger, HttpException, HttpStatus } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';
import { AxiosError } from 'axios';
import * as qs from 'qs'; // For application/x-www-form-urlencoded

// Assuming AppConfig is correctly typed and appConfig is loaded globally or provided
import appConfig, { AppConfig } from '../config/app.config';

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);
  private readonly apiGatewayUrl: string;
  private readonly oauthClientId: string;
  private readonly oauthClientSecret: string;

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService<AppConfig>, // Using NestJS ConfigService
  ) {
    // It's generally better to get these from ConfigService if it's setup globally
    // For this example, also showing direct import from app.config.ts as a fallback
    // const cfg = appConfig();
    // this.apiGatewayUrl = cfg.apiGatewayUrl;
    // this.oauthClientId = cfg.oauthClientId;
    // this.oauthClientSecret = cfg.oauthClientSecret;

    // Recommended: Using ConfigService (ensure ConfigModule.forRoot({ load: [appConfig], isGlobal: true }) in AppModule)
    this.apiGatewayUrl = this.configService.getOrThrow<string>('apiGatewayUrl', { infer: true });
    this.oauthClientId = this.configService.getOrThrow<string>('oauthClientId', { infer: true });
    this.oauthClientSecret = this.configService.getOrThrow<string>('oauthClientSecret', { infer: true });

    // The if block below is no longer needed as getOrThrow will handle missing configurations.
    // if (!this.apiGatewayUrl || !this.oauthClientId || !this.oauthClientSecret) {
    //  this.logger.error('Configuration missing: API Gateway URL or OAuth client details are not set.');
    //  throw new Error('Configuration missing for AuthService. Check environment variables or app.config.ts');
    // }
  }

  async login(username: string, password_val: string): Promise<any> {
    const tokenUrl = `${this.apiGatewayUrl}/auth/oauth2/token`; // Updated path
    const requestBody = {
      grant_type: 'password',
      username: username,
      password: password_val, // Renamed to avoid conflict with class property
      client_id: this.oauthClientId,
      client_secret: this.oauthClientSecret,
    };

    this.logger.debug(`Attempting login for user: ${username} to ${tokenUrl}`);

    try {
      const { data } = await firstValueFrom(
        this.httpService.post(tokenUrl, qs.stringify(requestBody), {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }),
      );
      this.logger.log(`Login successful for user: ${username}`);
      return data;
    } catch (error) {
      this.handleHttpError(error, `Login failed for user: ${username}`);
    }
  }

  async register(username: string, password_val: string, email: string): Promise<any> {
    const registerUrl = `${this.apiGatewayUrl}/users/register`; // Updated path
    const requestBody = { username, password: password_val, email }; // Renamed password to avoid conflict

    this.logger.debug(`Attempting registration for user: ${username} to ${registerUrl}`);

    try {
      const { data } = await firstValueFrom(
        this.httpService.post(registerUrl, requestBody, {
          headers: {
            'Content-Type': 'application/json',
          },
        }),
      );
      this.logger.log(`Registration successful for user: ${username}`);
      return data;
    } catch (error) {
      this.handleHttpError(error, `Registration failed for user: ${username}`);
    }
  }

  private handleHttpError(error: any, defaultMessage: string): never {
    if (error instanceof AxiosError && error.response) {
      this.logger.error(
        `${defaultMessage}. Status: ${error.response.status}, Data: ${JSON.stringify(error.response.data)}`,
      );
      throw new HttpException(
        error.response.data || defaultMessage,
        error.response.status || HttpStatus.INTERNAL_SERVER_ERROR,
      );
    } else {
      this.logger.error(`${defaultMessage}. Error: ${error.message}`, error.stack);
      throw new HttpException(
        defaultMessage,
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}
