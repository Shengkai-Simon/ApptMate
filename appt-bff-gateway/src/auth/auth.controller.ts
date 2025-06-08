import { Controller, Post, Body, Res, HttpStatus, HttpCode, UsePipes, ValidationPipe } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';
import { Response } from 'express'; // Import Response from express

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('login')
  @HttpCode(HttpStatus.OK) // Explicitly set success status code
  @UsePipes(new ValidationPipe({ transform: true, whitelist: true }))
  async login(@Body() loginDto: LoginDto, @Res({ passthrough: true }) response: Response) {
    const tokenData = await this.authService.login(loginDto.username, loginDto.password);

    // Set tokens in HTTP-only cookies
    // Adjust cookie options as needed for your security requirements (e.g., secure, sameSite)
    response.cookie('access_token', tokenData.access_token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production', // Use secure cookies in production
      sameSite: 'lax', // Or 'strict' or 'none' (if 'none', must be secure)
      maxAge: tokenData.expires_in * 1000, // expires_in is in seconds
      path: '/', // Cookie available for all paths
    });

    if (tokenData.refresh_token) {
      response.cookie('refresh_token', tokenData.refresh_token, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
        // Typically refresh token has a longer expiry
        // maxAge: refresh_token_expires_in * 1000, // if provided by auth server
        path: '/',
      });
    }

    // Return some user-related info or a success message
    // Avoid returning tokens in the response body if they are in cookies
    return {
      message: 'Login successful',
      // You might want to return some non-sensitive user info here if available
      // e.g., from a decoded access token or a separate user info endpoint call
    };
  }

  @Post('register')
  @UsePipes(new ValidationPipe({ transform: true, whitelist: true }))
  async register(@Body() registerDto: RegisterDto) {
    // The authService.register method will throw HttpException on failure
    return this.authService.register(registerDto.username, registerDto.password, registerDto.email);
  }
}
