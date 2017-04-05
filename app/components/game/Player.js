import React from 'react';

class Player extends React.Component {
  render() {
    const { avatar, chance } = this.props;
    return (
      <div className="infoUser">
        <img className="avatarHazard" src={avatar} />
        <span className="L-b">{chance}%</span>
      </div>
    );
  }
}

export default Player;