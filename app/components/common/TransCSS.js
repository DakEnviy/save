import React from 'react';

class TransCSS extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      currentClass: this.props.main + ' ' + this.props.name + '-enter'
    };
  }

  componentDidMount() {
    const self = this;
    setTimeout(() => {
      self.setState({
        currentClass: self.state.currentClass + ' ' + self.props.name + '-enter-active'
      });
      setTimeout(() => self.setState({ currentClass: self.props.main }), self.props.enterTimeout);
    }, 10);
  }

  render() {
    return (
      <span className={this.state.currentClass} style={this.props.style}>
        {this.props.children}
      </span>
    );
  }
}

export default TransCSS;