import { Injectable } from '@angular/core';
import { CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import {AuthService} from "../services/auth.service";


@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService) { }

  canActivate(route, state: RouterStateSnapshot)
  {
    this.authService.isLoggedIn.subscribe(data => {

      console.log('sssss::' + data);

      if (data)
      {
        return true;
      }
      else{
        this.router.navigate(['/login'], { queryParams:{ returnUrl: state.url }})
        return false;
      }
    });


    return true;
  }
}
