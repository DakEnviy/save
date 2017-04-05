import React, { PropTypes } from 'react';
import TransCSS from '../TransCSS';

class Modal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      style: {}
    };
  }

  componentWillMount() {
    this.setState({ style: { marginTop: document.body.scrollTop } });
  }

  handleBackgroundClick(e) {
    if (e.target === e.currentTarget) this.props.hideModal();
  }

  onOk() {
    this.props.onOk();
    this.props.hideModal();
  }

  render() {
    const okButton = this.props.showOk ? (
      <button className="modal-ok" onClick={this.onOk.bind(this)} disabled={this.props.okDisabled}>{this.props.okText}</button>
    ) : null;
    return (
      <TransCSS name="modal" main="modal-mnt" enterTimeout={400} leaveTimeout={400} style={this.state.style}>
        <div className="modal-bg"/>
        <div className="modal-wrap">
          <div className="modal-container" onClick={this.handleBackgroundClick.bind(this)}>
            <div className="modal-content" style={this.props.width !== 0 ? { width: this.props.width } : null}>
              <div className="modal-head">
                <h1>{this.props.title}</h1>
                <button className="modal-close" onClick={this.props.hideModal}>✕</button>
                {this.props.head}
              </div>
              <div className="modal-body">
                {this.props.children}
              </div>
              <div className="modal-footer">
                {okButton}
                {this.props.footer}
              </div>
            </div>
          </div>
        </div>
      </TransCSS>
    );
  }
}

Modal.propTypes = {
  modalId: PropTypes.string,
  title: PropTypes.string,
  head: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.element,
    PropTypes.string,
  ]),
  footer: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.element,
    PropTypes.string,
  ]),
  showOk: PropTypes.bool,
  okText: PropTypes.string,
  okDisabled: PropTypes.bool,
  width: PropTypes.number,
  children: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.element,
    PropTypes.string,
  ]).isRequired,
  hideModal: PropTypes.func,
  onOk: PropTypes.func,
};

Modal.defaultProps = {
  title: '',
  showOk: true,
  okText: 'OK',
  okDisabled: false,
  onOk: () => {}
};

export default Modal;