import React from 'react';
import { connect } from 'react-redux';
import ReactSpinner from 'react-spinjs';
import notify from '../actions/notify';
import 'whatwg-fetch';

class Achievements extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      packs: [],
      loaded: false,
      show: -1
    };
  }

  componentDidMount() {
    const self = this;
    fetch('/get_packs', {
      method: 'post',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin'
    })
    .then(res => {
      return res.json().then(data => {
        if (data.msg) self.props.dispatch(notify(data.msg));
        if (data.errs) self.props.dispatch(notify('LOAD_PACKS_ERROR'));
        if (data.success) {
          self.setState({
            packs: data.packs,
            loaded: true
          });
        } else self.setState({ loaded: true });
      });
    })
    .catch(err => {
      self.props.dispatch(notify('LOAD_PACKS_ERROR'));
      console.log(err);
    });
  }


  handleShow(i) {
    return e => {
      e.preventDefault();
      if (this.state.show === i) this.setState({ show: -1 });
      else this.setState({ show: i });
    };
  }

  render() {
    const { packs, loaded, show } = this.state;
    let listPacks = [];
    for (let i = 0, l = packs.length; i < l; i++) {
      const pack = packs[i];
      let listAchievements = [];
      for (let achievement of pack.achievements) {
        listAchievements.push(
          <div className="block-pack">
            <div className="pack">
              <img src={achievement.icon_url} />
              <div className="block">
                <div className="gradient"></div>
                <div className="info">
                  <span>{achievement.name}</span>
                  <span className="content">{achievement.description}</span>
                </div>
                <div className="gradient"></div>
              </div>
            </div>
          </div>
        );
      }
      listPacks.push(
        <div>
          <div className="name">
            <a href="#" onClick={this.handleShow(i).bind(this)}>{pack.name}</a>
          </div>
          {i === show && listAchievements}
        </div>
      );
    }
    return (
      <div className="blockGame">
        <div className="header">
          <div className="contentP">
            <h1 className="L-r">Ачивки</h1>
          </div>
        </div>
        <div className="AchievementsList">
          {loaded ? packs.length > 0 ?
              listPacks :
              <span style={{
                display: 'block',
                padding: '60px 0px 30px',
                textAlign: 'center',
                color: '#bbbbbb'
              }}>Нет паков достижений</span> : <ReactSpinner color="#a0a0a0"/>
          }
        </div>
      </div>
    );
  }
}

export default connect()(Achievements);