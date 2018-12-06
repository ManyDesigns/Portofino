import { MainPageDirective } from './content.directive';

describe('ContentDirective', () => {
  it('should create an instance', () => {
    const directive = new MainPageDirective(null);
    expect(directive).toBeTruthy();
  });
});
