import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import { Scrollbars } from 'react-custom-scrollbars';
import notify from './../actions/notify';

class Chat extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      show: false,
      messages: [],
      message: ''
    };
    this.onChatMessages = messages => this.setState({ messages });
    this.onNewMessages = messages => {
      this.setState({ messages }, () => {
        const node = ReactDOM.findDOMNode(this).children[2].children[0].children[0];
        node.scrollTop = node.scrollHeight;
      });
    };
  }

  componentDidMount() {
    window.socket.emit('chat_init');
    window.socket.on('chat_messages', this.onChatMessages);
    window.socket.on('new_messages', this.onNewMessages);
  }

  componentWillUnmount() {
    window.socket.removeListener('chat_messages', this.onChatMessages);
    window.socket.removeListener('new_messages', this.onNewMessages);
  }

  handleToggle() {
    this.setState({ show: !this.state.show });
  }

  handleChange(e) {
    this.setState({ [e.target.name]: e.target.value });
  }

  handleMessage(e) {
    if (!this.props.user.id) {
      this.props.dispatch(notify('INVALID_USER'));
      return e.preventDefault();
    }
    const { user } = this.props;
    const { message } = this.state;
    e.preventDefault();
    if (message.length <= 0) return this.props.dispatch(notify('MESSAGE_LENGTH'));
    window.socket.emit('send_message', {
      username: user.username,
      avatar: user.avatar,
      message: message
    });
    this.setState({ message: '' });
  }

  render() {
    const { show, messages, message } = this.state;
    return (
      <div className={'b-chat' + (show ? ' active' : '')}>
        <i className="fa fa-comments" onClick={this.handleToggle.bind(this)}/>
        <div className="b-header">
          <div>Чат</div>
        </div>
        <div className="message">
          <Scrollbars>
            {messages.map((message, key) => {
              return (
                <div key={key} className="b-user">
                  <div className="b-info">
                    <img className="u-avatar avatar" src={message.avatar}/>
                    <h1>{message.username}</h1>
                  </div>
                  <div className="gradient"></div>
                  <div className="b-text">{message.message}</div>
                  <div className="gradient"></div>
                </div>
              );
            })}
          </Scrollbars>
        </div>
        <form className="text-chat" onSubmit={this.handleMessage.bind(this)}>
          <input type="textarea" name="message" placeholder="Введите сообщение" value={message} onChange={this.handleChange.bind(this)}/>
          <button type="submit"><i className="fa fa-weixin"/></button>
        </form>
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    user: state.user
  };
};

export default connect(mapStateToProps)(Chat);