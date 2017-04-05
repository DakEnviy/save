import React from 'react';
import { Link, browserHistory } from 'react-router';
import { connect } from 'react-redux';
import * as modals from '../actions/modal';

class Header extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      show_select: false
    };
  }

  redirectToProfile() {
    // browserHistory.push('/' + this.props.gameName + '/profile');
    // this.props.handleIndex();
    location.href = '/' + this.props.gameName + '/profile';
  }

  handleShowSelect(e) {
    e.preventDefault();
    this.setState({ show_select: !this.state.show_select });
  }

  render() {
    const { user, gameName, path, handleIndex } = this.props;
    const { show_select } = this.state;
    return (
      <header>
        <Link to={'/' + gameName} onClick={handleIndex(gameName)}><img src="/img/logo-1.png" className="logo-csgo logo-img"/></Link>
        <Link to={'/' + gameName} onClick={handleIndex(gameName)}><img src="/img/logo-2.png" className="logo-dota logo-img"/></Link>
        <div className="logo">
          <h1><Link to={'/' + gameName} onClick={handleIndex(gameName)}><span>A</span>WAPA.RU</Link></h1>
          <h2></h2>
          <div className="a-menu">
            <a href="#" className={path === '/' ? 'active' : null} onClick={this.handleShowSelect.bind(this)}>
              <i className="fa fa-gamepad"/>
            </a>
            <Link to={'/' + gameName + '/shop'} activeClassName="active">
              <i className="fa fa-shopping-basket"/>
            </Link>
            <Link to={'/' + gameName + '/history'} activeClassName="active">
              <i className="fa fa-history"/>
            </Link>
            <Link to={'/' + gameName + '/top'} activeClassName="active">
              <i className="fa fa-star-half-o"/>
            </Link>
            <Link to={'/' + gameName + '/profile'} activeClassName="active" onClick={this.redirectToProfile}>
              <i className="fa fa-user"/>
            </Link>
          </div>
        </div>
        <div className="Block-B" style={show_select ? {display: 'block'} : null}>
          <div className="gradient"/>
          <Link to="/csgo" className="button" activeClassName="active" onClick={handleIndex('csgo')}>
            <h1>CSGO</h1>
            <img src="/img/csgo3.jpeg" className="block"/>
          </Link>
          <Link to="/dota" className="button" activeClassName="active" onClick={handleIndex('dota')}>
            <h1>DOTA</h1>
            <img src="/img/dota.jpeg" className="block"/>
          </Link>
          <div className="gradient"/>
        </div>
        {user.id ?
          <div className="ProfileSteam">
            <div className="SteamInfo">
              <span className="Name" onClick={this.redirectToProfile}>{user.username}</span>
              <span className="Balance">
                Баланс: <b>{user.money} р</b>
                <i className="fa fa-plus-square" onClick={() => this.props.dispatch(modals.paymentModal())} />
              </span>
              <a href="/logout" className="logOut">Выйти</a>
            </div>
            <img src={user.avatar} className="avatar" onClick={this.redirectToProfile}/>
          </div>
          :
          <div className="LogSteam">
            <div className="SteamInfo">
              <a href="/auth/steam">
                <span className="log">Войти в профиль</span>
              </a>
            </div>
            <div className="block-i">
              <i className="fa fa-sign-out"/>
            </div>
          </div>
        }
      </header>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    user: state.user
  };
};

export default connect(mapStateToProps)(Header);
